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
import java.text.SimpleDateFormat
import java.util.*

class AddPerformanceViewModel(
    private val repository: SingventoryRepository,
    private val visitId: Long
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _visitDetails = MutableStateFlow<VisitDetailsData?>(null)
    val visitDetails: StateFlow<VisitDetailsData?> = _visitDetails.asStateFlow()
    
    private val _venue = MutableStateFlow<com.pseddev.singventory.data.entity.Venue?>(null)
    val venue: StateFlow<com.pseddev.singventory.data.entity.Venue?> = _venue.asStateFlow()
    
    private val _songSearchQuery = MutableStateFlow("")
    val songSearchQuery: StateFlow<String> = _songSearchQuery.asStateFlow()
    
    private val _saveResult = MutableStateFlow<Result<Unit>?>(null)
    val saveResult: StateFlow<Result<Unit>?> = _saveResult.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    
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
        loadVisitDetails()
    }
    
    private fun loadVisitDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val visit = repository.getVisitById(visitId)
                visit?.let { v ->
                    val venue = repository.getVenueById(v.venueId)
                    _venue.value = venue
                    
                    val visitDate = Date(v.timestamp)
                    _visitDetails.value = VisitDetailsData(
                        visit = v,
                        venueName = venue?.name ?: "Unknown Venue",
                        formattedDateTime = dateFormat.format(visitDate)
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
    
    fun savePerformance(
        song: Song,
        timestamp: Long,
        keyAdjustment: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val performance = Performance(
                    id = 0, // Auto-generated
                    visitId = visitId,
                    songId = song.id,
                    timestamp = timestamp,
                    keyAdjustment = keyAdjustment,
                    notes = notes
                )
                
                // Use repository's logPerformance method to ensure statistics are updated
                repository.logPerformance(performance)
                
                _saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearSaveResult() {
        _saveResult.value = null
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val visitId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddPerformanceViewModel::class.java)) {
                return AddPerformanceViewModel(repository, visitId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}