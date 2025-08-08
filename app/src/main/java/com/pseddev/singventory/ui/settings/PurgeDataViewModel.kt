package com.pseddev.singventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PurgeDataOverview(
    val totalVisits: Int = 0,
    val totalPerformances: Int = 0,
    val oldestVisitDate: String = "N/A",
    val newestVisitDate: String = "N/A",
    val recommendedPurgeCount: Int = 0,
    val estimatedSpaceSavings: String = "Unknown"
)

sealed class PurgeState {
    object Idle : PurgeState()
    data class Success(
        val deletedVisits: Int,
        val deletedPerformances: Int
    ) : PurgeState()
    object NoVisitsToPurge : PurgeState()
    data class Error(val message: String) : PurgeState()
}

class PurgeDataViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _dataOverview = MutableStateFlow(PurgeDataOverview())
    val dataOverview: StateFlow<PurgeDataOverview> = _dataOverview.asStateFlow()
    
    private val _purgeState = MutableStateFlow<PurgeState>(PurgeState.Idle)
    val purgeState: StateFlow<PurgeState> = _purgeState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun resetStates() {
        _purgeState.value = PurgeState.Idle
        _isLoading.value = false
    }
    
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    fun loadDataOverview() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val visits = repository.getAllVisits().first()
                val performances = repository.getAllPerformances().first()
                
                val sortedVisits = visits.sortedBy { it.timestamp }
                val oldestVisit = sortedVisits.firstOrNull()
                val newestVisit = sortedVisits.lastOrNull()
                
                val recommendedPurgeCount = calculateRecommendedPurgeCount(visits.size)
                
                _dataOverview.value = PurgeDataOverview(
                    totalVisits = visits.size,
                    totalPerformances = performances.size,
                    oldestVisitDate = oldestVisit?.let { 
                        dateFormat.format(Date(it.timestamp)) 
                    } ?: "N/A",
                    newestVisitDate = newestVisit?.let { 
                        dateFormat.format(Date(it.timestamp)) 
                    } ?: "N/A",
                    recommendedPurgeCount = recommendedPurgeCount,
                    estimatedSpaceSavings = estimateSpaceSavings(recommendedPurgeCount, performances.size)
                )
            } catch (e: Exception) {
                // Handle error silently, keep default overview
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun analyzeDataForPurging() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val visits = repository.getAllVisits().first()
                val performances = repository.getAllPerformances().first()
                
                // Detailed analysis could go here
                // For now, just refresh the overview
                loadDataOverview()
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun purgeOldestVisits(count: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _purgeState.value = PurgeState.Idle
            
            try {
                val result = repository.purgeOldestVisits(count)
                
                when (result) {
                    is SingventoryRepository.PurgeResult.Success -> {
                        _purgeState.value = PurgeState.Success(
                            deletedVisits = result.deletedVisits,
                            deletedPerformances = result.deletedPerformances
                        )
                    }
                    is SingventoryRepository.PurgeResult.NoVisitsToPurge -> {
                        _purgeState.value = PurgeState.NoVisitsToPurge
                    }
                    is SingventoryRepository.PurgeResult.Error -> {
                        _purgeState.value = PurgeState.Error(result.message)
                    }
                }
                
            } catch (e: Exception) {
                _purgeState.value = PurgeState.Error(e.message ?: "Unknown purge error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getRecommendedPurgeCount(): Int? {
        val currentOverview = _dataOverview.value
        return if (currentOverview.recommendedPurgeCount > 0) {
            currentOverview.recommendedPurgeCount
        } else null
    }
    
    private fun calculateRecommendedPurgeCount(totalVisits: Int): Int {
        return when {
            totalVisits > 1000 -> 200  // Purge 200 if over 1000 visits
            totalVisits > 750 -> 150   // Purge 150 if over 750 visits
            totalVisits > 500 -> 100   // Purge 100 if over 500 visits (our soft limit)
            else -> 0                  // No purging recommended
        }
    }
    
    private fun estimateSpaceSavings(purgeCount: Int, totalPerformances: Int): String {
        if (purgeCount == 0) return "No purging recommended"
        
        // Rough estimate: each visit ~1KB, each performance ~0.5KB
        val estimatedBytes = (purgeCount * 1024) + (totalPerformances * 0.3 * purgeCount).toInt()
        
        return when {
            estimatedBytes > 1024 * 1024 -> "${estimatedBytes / (1024 * 1024)}MB"
            estimatedBytes > 1024 -> "${estimatedBytes / 1024}KB" 
            else -> "${estimatedBytes}B"
        }
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PurgeDataViewModel::class.java)) {
                return PurgeDataViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}