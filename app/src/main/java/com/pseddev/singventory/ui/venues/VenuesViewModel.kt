package com.pseddev.singventory.ui.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class VenuesViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Combine all venues with search query to create filtered list
    val venues = combine(
        repository.getAllVenues(),
        _searchQuery
    ) { allVenues, query ->
        if (query.isBlank()) {
            // No search - sort by most visited (PlayStreak pattern)
            allVenues.sortedByDescending { it.totalVisits }
        } else {
            // Search functionality prioritizing frequently visited venues
            allVenues.filter { venue ->
                venue.name.contains(query, ignoreCase = true) ||
                venue.address?.contains(query, ignoreCase = true) == true
            }.sortedByDescending { it.totalVisits }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    suspend fun getVenueById(venueId: Long) = repository.getVenueById(venueId)
    
    fun deleteVenue(venue: Venue) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteVenue(venue)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VenuesViewModel::class.java)) {
                return VenuesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}