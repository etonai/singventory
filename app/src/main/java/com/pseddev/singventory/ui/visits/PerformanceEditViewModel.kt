package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Performance
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    
    private val timeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    
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
    
    fun saveChanges(keyAdjustment: Int, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDetails = _performanceDetails.value ?: return@launch
                val updatedPerformance = currentDetails.performance.copy(
                    keyAdjustment = keyAdjustment,
                    notes = notes
                )
                
                repository.updatePerformance(updatedPerformance)
                
                // Update local state
                _performanceDetails.value = currentDetails.copy(performance = updatedPerformance)
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
                repository.deletePerformance(currentDetails.performance)
            } finally {
                _isLoading.value = false
            }
        }
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