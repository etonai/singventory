package com.pseddev.singventory.ui.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditSongVenueInfoDetails(
    val songVenueInfo: SongVenueInfo,
    val songName: String,
    val artistName: String,
    val venueName: String,
    val performanceCount: Int,
    val lyrics: String?
)

class EditSongVenueInfoViewModel(
    private val repository: SingventoryRepository,
    private val songVenueInfoId: Long
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _songVenueInfoDetails = MutableStateFlow<EditSongVenueInfoDetails?>(null)
    val songVenueInfoDetails: StateFlow<EditSongVenueInfoDetails?> = _songVenueInfoDetails.asStateFlow()
    
    init {
        loadSongVenueInfoDetails()
    }
    
    private fun loadSongVenueInfoDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val songVenueInfo = repository.getSongVenueInfoById(songVenueInfoId)
                songVenueInfo?.let { svi ->
                    val song = repository.getSongById(svi.songId)
                    val venue = repository.getVenueById(svi.venueId)
                    val performanceCount = repository.getPerformanceCountForSongAtVenue(
                        svi.songId, 
                        svi.venueId
                    )
                    
                    _songVenueInfoDetails.value = EditSongVenueInfoDetails(
                        songVenueInfo = svi,
                        songName = song?.name ?: "Unknown Song",
                        artistName = song?.artist ?: "",
                        venueName = venue?.name ?: "Unknown Venue",
                        performanceCount = performanceCount,
                        lyrics = song?.lyrics
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveChanges(venueSongNumber: String?, venueKey: String?, keyAdjustment: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDetails = _songVenueInfoDetails.value ?: return@launch
                val updatedSongVenueInfo = currentDetails.songVenueInfo.copy(
                    venuesSongId = venueSongNumber,
                    venueKey = venueKey,
                    keyAdjustment = keyAdjustment
                )
                
                repository.updateSongVenueInfo(updatedSongVenueInfo)
                
                // Update local state
                _songVenueInfoDetails.value = currentDetails.copy(songVenueInfo = updatedSongVenueInfo)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAssociation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDetails = _songVenueInfoDetails.value ?: return@launch
                repository.deleteSongVenueInfo(currentDetails.songVenueInfo)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val songVenueInfoId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditSongVenueInfoViewModel::class.java)) {
                return EditSongVenueInfoViewModel(repository, songVenueInfoId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}