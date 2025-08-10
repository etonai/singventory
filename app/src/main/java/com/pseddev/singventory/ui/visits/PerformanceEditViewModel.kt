package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Performance
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PerformanceEditData(
    val performance: Performance,
    val songName: String,
    val artistName: String,
    val formattedTime: String
)

class PerformanceEditViewModel(
    private val repository: SingventoryRepository,
    private val performanceId: Long
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _performanceDetails = MutableStateFlow<PerformanceEditData?>(null)
    val performanceDetails: StateFlow<PerformanceEditData?> = _performanceDetails.asStateFlow()
    
    private val _venue = MutableStateFlow<com.pseddev.singventory.data.entity.Venue?>(null)
    val venue: StateFlow<com.pseddev.singventory.data.entity.Venue?> = _venue.asStateFlow()
    
    private val _songSearchQuery = MutableStateFlow("")
    val songSearchQuery: StateFlow<String> = _songSearchQuery.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult.asStateFlow()
    
    private val timeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    
    // Get all songs for search mode
    val allSongs = repository.getAllSongs()
    
    // Filtered songs based on search query
    val filteredSongs = combine(
        allSongs,
        _songSearchQuery
    ) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.name.contains(query, ignoreCase = true) ||
                song.artist?.contains(query, ignoreCase = true) == true
            }
        }
    }
    
    // Get songs with venue-specific information for dropdown mode
    val songsWithVenueInfo = combine(
        repository.getAllSongs(),
        _venue
    ) { songs, venue ->
        if (venue == null) {
            songs.map { song ->
                SongWithVenueInfo(
                    song = song,
                    keyAdjustment = null
                )
            }
        } else {
            songs.map { song ->
                val venueInfo = repository.getSongVenueInfo(song.id, venue.id)
                SongWithVenueInfo(
                    song = song,
                    keyAdjustment = venueInfo?.keyAdjustment
                )
            }
        }
    }
    
    init {
        loadPerformanceDetails()
    }
    
    private fun loadPerformanceDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val performance = repository.getPerformanceById(performanceId)
                performance?.let { perf ->
                    val song = repository.getSongById(perf.songId)
                    val performanceTime = Date(perf.timestamp)
                    
                    // Load venue information for song selection
                    val visit = repository.getVisitById(perf.visitId)
                    visit?.let { v ->
                        val venue = repository.getVenueById(v.venueId)
                        _venue.value = venue
                    }
                    
                    _performanceDetails.value = PerformanceEditData(
                        performance = perf,
                        songName = song?.name ?: "Unknown Song",
                        artistName = song?.artist ?: "",
                        formattedTime = timeFormat.format(performanceTime)
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSongSearchQuery(query: String) {
        _songSearchQuery.value = query
    }
    
    fun saveChanges(
        song: Song?,
        timestamp: Long,
        keyAdjustment: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDetails = _performanceDetails.value ?: return@launch
                val updatedPerformance = currentDetails.performance.copy(
                    songId = song?.id ?: currentDetails.performance.songId,
                    timestamp = timestamp,
                    keyAdjustment = keyAdjustment,
                    notes = notes
                )
                
                // Update performance with proper statistics handling
                repository.updatePerformanceWithStats(currentDetails.performance, updatedPerformance)
                
                // Update local state with new song information if song changed
                val newSongName = song?.name ?: currentDetails.songName
                val newArtistName = song?.artist ?: currentDetails.artistName
                val newFormattedTime = timeFormat.format(Date(timestamp))
                
                _performanceDetails.value = currentDetails.copy(
                    performance = updatedPerformance,
                    songName = newSongName,
                    artistName = newArtistName,
                    formattedTime = newFormattedTime
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deletePerformance() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDetails = _performanceDetails.value ?: return@launch
                repository.deletePerformanceWithStats(currentDetails.performance)
                _deleteResult.value = true
            } catch (e: Exception) {
                _deleteResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val performanceId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerformanceEditViewModel::class.java)) {
                return PerformanceEditViewModel(repository, performanceId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}