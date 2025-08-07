package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Performance
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.entity.Visit
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ActiveVisitViewModel(
    private val repository: SingventoryRepository,
    private val visitId: Long
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _visit = MutableStateFlow<Visit?>(null)
    val visit: StateFlow<Visit?> = _visit.asStateFlow()
    
    private val _venue = MutableStateFlow<com.pseddev.singventory.data.entity.Venue?>(null)
    val venue: StateFlow<com.pseddev.singventory.data.entity.Venue?> = _venue.asStateFlow()
    
    private val _songSearchQuery = MutableStateFlow("")
    val songSearchQuery: StateFlow<String> = _songSearchQuery.asStateFlow()
    
    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong: StateFlow<Song?> = _selectedSong.asStateFlow()
    
    private val _quickLogShowing = MutableStateFlow(false)
    val quickLogShowing: StateFlow<Boolean> = _quickLogShowing.asStateFlow()
    
    // Get all songs for autocomplete
    val allSongs = repository.getAllSongs()
    
    // Get performances for this visit with song details
    val performances = repository.getPerformancesByVisit(visitId).map { performanceList ->
        performanceList.map { performance ->
            val song = repository.getSongById(performance.songId)
            PerformanceWithSong(
                performance = performance,
                songName = song?.name ?: "Unknown Song",
                artistName = song?.artist ?: ""
            )
        }.sortedByDescending { it.performance.timestamp } // Most recent first
    }
    
    init {
        loadVisitDetails()
    }
    
    private fun loadVisitDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val visit = repository.getVisitById(visitId)
                _visit.value = visit
                
                visit?.let { v ->
                    val venue = repository.getVenueById(v.venueId)
                    _venue.value = venue
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSongSearchQuery(query: String) {
        _songSearchQuery.value = query
    }
    
    fun selectSong(song: Song) {
        _selectedSong.value = song
    }
    
    fun clearSelectedSong() {
        _selectedSong.value = null
        _songSearchQuery.value = ""
    }
    
    fun showQuickLog() {
        _quickLogShowing.value = true
    }
    
    fun hideQuickLog() {
        _quickLogShowing.value = false
        clearSelectedSong()
    }
    
    suspend fun getSongByNameAndArtist(name: String, artist: String): Song? {
        return repository.getSongByNameAndArtist(name, artist)
    }
    
    suspend fun createQuickSong(songName: String, artistName: String? = null): Song? {
        return try {
            val song = Song(
                name = songName.trim(),
                artist = artistName?.trim() ?: "",
                referenceKey = null,
                preferredKey = null,
                lyrics = null
            )
            
            val songId = repository.insertSong(song)
            val createdSong = song.copy(id = songId)
            _selectedSong.value = createdSong
            createdSong
        } catch (e: Exception) {
            // Handle error
            null
        }
    }
    
    suspend fun createSongAndLog(songName: String, artistName: String? = null, keyAdjustment: Int = 0, notes: String? = null): Boolean {
        val createdSong = createQuickSong(songName, artistName)
        return if (createdSong != null) {
            logPerformanceSync(keyAdjustment, notes)
            true
        } else {
            false
        }
    }
    
    private suspend fun logPerformanceSync(keyAdjustment: Int = 0, notes: String? = null): Boolean {
        val song = _selectedSong.value ?: return false
        
        return try {
            repository.logPerformance(
                visitId = visitId,
                songId = song.id,
                keyAdjustment = keyAdjustment,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                timestamp = System.currentTimeMillis()
            )
            
            // Clear selection after logging
            clearSelectedSong()
            _quickLogShowing.value = false
            true
        } catch (e: Exception) {
            // Handle error
            false
        }
    }
    
    fun logPerformance(keyAdjustment: Int = 0, notes: String? = null) {
        viewModelScope.launch {
            logPerformanceSync(keyAdjustment, notes)
        }
    }
    
    fun endVisit(notes: String? = null, amountSpent: Double? = null) {
        viewModelScope.launch {
            try {
                val currentVisit = _visit.value ?: return@launch
                val endedVisit = currentVisit.copy(
                    endTimestamp = System.currentTimeMillis(),
                    isActive = false,
                    notes = notes?.trim()?.takeIf { it.isNotBlank() },
                    amountSpent = amountSpent
                )
                
                repository.updateVisit(endedVisit)
                _visit.value = endedVisit
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val visitId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActiveVisitViewModel::class.java)) {
                return ActiveVisitViewModel(repository, visitId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}