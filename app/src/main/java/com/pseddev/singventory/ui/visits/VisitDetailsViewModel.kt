package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.dao.PerformanceWithSongInfo
import com.pseddev.singventory.data.entity.Visit
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class VisitDetailsData(
    val visit: Visit,
    val venueName: String,
    val formattedDateTime: String
)


class VisitDetailsViewModel(
    private val repository: SingventoryRepository,
    private val visitId: Long
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _visitDetails = MutableStateFlow<VisitDetailsData?>(null)
    val visitDetails: StateFlow<VisitDetailsData?> = _visitDetails.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult.asStateFlow()
    
    // Get performances for this visit with song details using JOIN query
    val performances = repository.getPerformancesByVisitWithSongInfo(visitId).map { performanceList ->
        performanceList.map { performanceWithSongInfo ->
            PerformanceWithSong(
                performance = performanceWithSongInfo.performance,
                songName = performanceWithSongInfo.songName,
                artistName = performanceWithSongInfo.artistName
            )
        }.sortedByDescending { it.performance.timestamp } // Most recent first
    }
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    
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
    
    fun saveVisitChanges(notes: String?, amountSpent: Double?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentVisit = _visitDetails.value?.visit ?: return@launch
                val updatedVisit = currentVisit.copy(
                    notes = notes,
                    amountSpent = amountSpent
                )
                
                repository.updateVisit(updatedVisit)
                
                // Update local state
                _visitDetails.value = _visitDetails.value?.copy(visit = updatedVisit)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteVisit() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentVisit = _visitDetails.value?.visit ?: return@launch
                repository.deleteVisitWithStats(currentVisit)
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
        private val visitId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VisitDetailsViewModel::class.java)) {
                return VisitDetailsViewModel(repository, visitId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}