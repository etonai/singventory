package com.pseddev.singventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.BuildConfig
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.entity.Visit
import com.pseddev.singventory.data.entity.Performance
import com.pseddev.singventory.data.entity.SongVenueInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DataStatistics(
    val songsCount: Int = 0,
    val venuesCount: Int = 0,
    val visitsCount: Int = 0,
    val performancesCount: Int = 0,
    val associationsCount: Int = 0,
    val visitFrequencyDays: Double = 0.0 // average days between visits
)


class SettingsViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _dataStatistics = MutableStateFlow(DataStatistics())
    val dataStatistics: StateFlow<DataStatistics> = _dataStatistics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _clearDataEnabled = MutableStateFlow<Boolean>(BuildConfig.DEBUG)
    val clearDataEnabled: StateFlow<Boolean> = _clearDataEnabled.asStateFlow()
    
    init {
        loadDataStatistics()
    }
    
    private fun loadDataStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Combine all data sources to calculate comprehensive statistics
                combine(
                    repository.getAllSongs(),
                    repository.getAllVenues(),
                    repository.getAllVisits(),
                    repository.getAllPerformances(),
                    repository.getAllSongVenueInfo()
                ) { songs, venues, visits, performances, associations ->
                    
                    // Calculate basic counts
                    val songsCount = songs.size
                    val venuesCount = venues.size
                    val visitsCount = visits.size
                    val performancesCount = performances.size
                    val associationsCount = associations.size
                    
                    // Calculate visit frequency (average days between visits)
                    val visitFrequencyDays = if (visits.size >= 2) {
                        val sortedVisits = visits.sortedBy { it.timestamp }
                        val totalDays = (sortedVisits.last().timestamp - sortedVisits.first().timestamp) / (1000 * 60 * 60 * 24)
                        totalDays.toDouble() / (visits.size - 1)
                    } else 0.0
                    
                    DataStatistics(
                        songsCount = songsCount,
                        venuesCount = venuesCount,
                        visitsCount = visitsCount,
                        performancesCount = performancesCount,
                        associationsCount = associationsCount,
                        visitFrequencyDays = visitFrequencyDays
                    )
                }.collect { statistics ->
                    _dataStatistics.value = statistics
                }
                
            } catch (e: Exception) {
                // Handle error, keep previous statistics
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            if (!BuildConfig.DEBUG) return@launch // Safety check
            
            _isLoading.value = true
            try {
                repository.clearAllData()
                // Refresh statistics after clearing
                loadDataStatistics()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshData() {
        loadDataStatistics()
    }
    
    
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}