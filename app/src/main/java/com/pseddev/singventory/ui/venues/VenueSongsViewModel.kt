package com.pseddev.singventory.ui.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SongVenueInfoWithDetails(
    val songVenueInfo: SongVenueInfo,
    val songName: String,
    val artistName: String,
    val performanceCount: Int
) {
    val id: Long get() = songVenueInfo.id
}

class VenueSongsViewModel(
    private val repository: SingventoryRepository,
    private val venueId: Long,
    private val contextVisitId: Long? = null
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _venueInfo = MutableStateFlow<Venue?>(null)
    val venueInfo: StateFlow<Venue?> = _venueInfo.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _hasActiveVisit = MutableStateFlow(false)
    val hasActiveVisit: StateFlow<Boolean> = _hasActiveVisit.asStateFlow()
    
    private val _hasVisitContext = MutableStateFlow(false)
    val hasVisitContext: StateFlow<Boolean> = _hasVisitContext.asStateFlow()
    
    private val _activeVisit = MutableStateFlow<com.pseddev.singventory.data.entity.Visit?>(null)
    val activeVisit: StateFlow<com.pseddev.singventory.data.entity.Visit?> = _activeVisit.asStateFlow()
    
    // Get all song-venue associations for this venue with song details using efficient JOIN query
    private val allSongs = repository.getSongVenueInfoWithSongDetailsByVenue(venueId).map { songVenueInfoWithDetailsList ->
        songVenueInfoWithDetailsList.map { songVenueInfoWithDetails ->
            // Convert flattened data back to our ViewModel data class
            val songVenueInfo = com.pseddev.singventory.data.entity.SongVenueInfo(
                id = songVenueInfoWithDetails.id,
                songId = songVenueInfoWithDetails.songId,
                venueId = songVenueInfoWithDetails.venueId,
                venuesSongId = songVenueInfoWithDetails.venuesSongId,
                venueKey = songVenueInfoWithDetails.venueKey,
                keyAdjustment = songVenueInfoWithDetails.keyAdjustment,
                lyrics = songVenueInfoWithDetails.lyrics,
                performanceCount = songVenueInfoWithDetails.performanceCount,
                lastPerformed = songVenueInfoWithDetails.lastPerformed
            )
            
            SongVenueInfoWithDetails(
                songVenueInfo = songVenueInfo,
                songName = songVenueInfoWithDetails.songName,
                artistName = songVenueInfoWithDetails.artistName,
                performanceCount = songVenueInfoWithDetails.performanceCount // Use the actual performance count from database
            )
        } // Already sorted alphabetically by song name in the query
    }
    
    // Filtered songs based on search query
    val filteredSongs = combine(allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            songs.filter { songInfo ->
                songInfo.songName.contains(query, ignoreCase = true) ||
                songInfo.artistName.contains(query, ignoreCase = true) ||
                songInfo.songVenueInfo.venuesSongId?.contains(query, ignoreCase = true) == true
            }
        }
    }
    
    init {
        loadVenueInfo()
        checkActiveVisitStatus()
    }
    
    private fun loadVenueInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val venue = repository.getVenueById(venueId)
                _venueInfo.value = venue
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    suspend fun getActiveVisitForVenue(): com.pseddev.singventory.data.entity.Visit? {
        return if (contextVisitId != null) {
            // Return the specific visit context provided
            _activeVisit.value
        } else {
            // Fall back to auto-detecting active visit
            _activeVisit.value ?: repository.getActiveVisitForVenue(venueId)
        }
    }
    
    private fun checkActiveVisitStatus() {
        viewModelScope.launch {
            if (contextVisitId != null) {
                // Use specific visit context
                val visit = repository.getVisitById(contextVisitId)
                _activeVisit.value = visit
                // Show active visit indicator only if this specific visit is actually active
                _hasActiveVisit.value = visit?.endTimestamp == null
                // Show visit context (for + icons) whenever we have any visit context
                _hasVisitContext.value = visit != null
            } else {
                // Fall back to detecting active visit for venue
                val activeVisit = repository.getActiveVisitForVenue(venueId)
                _activeVisit.value = activeVisit
                _hasActiveVisit.value = activeVisit != null
                // Only show visit context when there's an active visit
                _hasVisitContext.value = activeVisit != null
            }
        }
    }
    
    fun deleteSongVenueInfo(songVenueInfoWithDetails: SongVenueInfoWithDetails) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSongVenueInfo(songVenueInfoWithDetails.songVenueInfo)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun logPerformance(songId: Long, keyAdjustment: Int = 0, notes: String? = null): Boolean {
        return try {
            val visitForLogging = _activeVisit.value
            if (visitForLogging != null) {
                repository.logPerformance(
                    visitId = visitForLogging.id,
                    songId = songId,
                    keyAdjustment = keyAdjustment,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getSongVenueInfo(songId: Long): SongVenueInfo? {
        return try {
            repository.getSongVenueInfo(songId, venueId)
        } catch (e: Exception) {
            null
        }
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val venueId: Long,
        private val contextVisitId: Long? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VenueSongsViewModel::class.java)) {
                return VenueSongsViewModel(repository, venueId, contextVisitId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}