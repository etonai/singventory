package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.entity.Visit
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartVisitViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _selectedVenue = MutableStateFlow<Venue?>(null)
    val selectedVenue: StateFlow<Venue?> = _selectedVenue.asStateFlow()
    
    private val _visitNotes = MutableStateFlow<String?>(null)
    val visitNotes: StateFlow<String?> = _visitNotes.asStateFlow()
    
    private val _isCreatingVisit = MutableStateFlow(false)
    val isCreatingVisit: StateFlow<Boolean> = _isCreatingVisit.asStateFlow()
    
    private val _visitCreated = MutableStateFlow<Visit?>(null)
    val visitCreated: StateFlow<Visit?> = _visitCreated.asStateFlow()
    
    private val _showCreateVenue = MutableStateFlow(false)
    val showCreateVenue: StateFlow<Boolean> = _showCreateVenue.asStateFlow()
    
    // Get all venues for dropdown population
    val allVenues = repository.getAllVenues()
    
    fun selectVenue(venue: Venue) {
        _selectedVenue.value = venue
        _showCreateVenue.value = false
    }
    
    fun clearSelectedVenue() {
        _selectedVenue.value = null
    }
    
    fun setVisitNotes(notes: String?) {
        _visitNotes.value = notes?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun showCreateNewVenue() {
        _showCreateVenue.value = true
    }
    
    fun hideCreateVenue() {
        _showCreateVenue.value = false
    }
    
    suspend fun getVenueByName(name: String): Venue? {
        return repository.getVenueByName(name)
    }
    
    fun createQuickVenue(venueName: String) {
        viewModelScope.launch {
            try {
                // Create a minimal venue with just the name
                val venue = Venue(
                    name = venueName.trim(),
                    address = null,
                    cost = null,
                    roomType = null,
                    hours = null,
                    notes = null
                )
                
                val venueId = repository.insertVenue(venue)
                val createdVenue = venue.copy(id = venueId)
                _selectedVenue.value = createdVenue
                _showCreateVenue.value = false
            } catch (e: Exception) {
                // Handle error - could emit error state
            }
        }
    }
    
    fun startVisit() {
        val venue = _selectedVenue.value
        if (venue == null) return
        
        viewModelScope.launch {
            _isCreatingVisit.value = true
            try {
                val visit = Visit(
                    venueId = venue.id,
                    timestamp = System.currentTimeMillis(),
                    endTimestamp = null,
                    notes = _visitNotes.value,
                    amountSpent = null,
                    isActive = true
                )
                
                val visitId = repository.insertVisit(visit)
                val createdVisit = visit.copy(id = visitId)
                _visitCreated.value = createdVisit
                
            } catch (e: Exception) {
                // Handle error - could emit error state  
            } finally {
                _isCreatingVisit.value = false
            }
        }
    }
    
    fun clearVisitCreated() {
        _visitCreated.value = null
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartVisitViewModel::class.java)) {
                return StartVisitViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}