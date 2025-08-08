package com.pseddev.singventory.ui.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditVenueViewModel(
    private val repository: SingventoryRepository,
    private val venueId: Long
) : ViewModel() {
    
    private val _venue = MutableStateFlow<Venue?>(null)
    val venue: StateFlow<Venue?> = _venue.asStateFlow()
    
    private val _venueName = MutableStateFlow("")
    val venueName: StateFlow<String> = _venueName.asStateFlow()
    
    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()
    
    private val _roomType = MutableStateFlow<String?>(null)
    val roomType: StateFlow<String?> = _roomType.asStateFlow()
    
    private val _cost = MutableStateFlow<String?>(null)
    val cost: StateFlow<String?> = _cost.asStateFlow()
    
    private val _hours = MutableStateFlow<String?>(null)
    val hours: StateFlow<String?> = _hours.asStateFlow()
    
    private val _notes = MutableStateFlow<String?>(null)
    val notes: StateFlow<String?> = _notes.asStateFlow()
    
    private val _songsAssociated = MutableStateFlow(0)
    val songsAssociated: StateFlow<Int> = _songsAssociated.asStateFlow()
    
    private val _totalVisits = MutableStateFlow(0)
    val totalVisits: StateFlow<Int> = _totalVisits.asStateFlow()
    
    private val _lastVisited = MutableStateFlow<String?>(null)
    val lastVisited: StateFlow<String?> = _lastVisited.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult.asStateFlow()
    
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()
    
    init {
        loadVenue()
    }
    
    private fun loadVenue() {
        viewModelScope.launch {
            try {
                val venueData = repository.getVenueById(venueId)
                if (venueData != null) {
                    _venue.value = venueData
                    _venueName.value = venueData.name
                    _address.value = venueData.address
                    _roomType.value = venueData.roomType
                    _cost.value = venueData.cost
                    _hours.value = venueData.hours
                    _notes.value = venueData.notes
                    
                    // Load statistics
                    _songsAssociated.value = repository.getSongCountForVenue(venueId)
                    
                    // Load visit statistics (placeholder for now)
                    _totalVisits.value = 0
                    _lastVisited.value = null
                }
            } catch (e: Exception) {
                // Handle error - venue not found
            }
        }
    }
    
    fun setVenueName(name: String) {
        _venueName.value = name.trim()
        checkForChanges()
    }
    
    fun setAddress(address: String?) {
        _address.value = address?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    fun setRoomType(roomType: String?) {
        _roomType.value = roomType?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    fun setCost(cost: String?) {
        _cost.value = cost?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    fun setHours(hours: String?) {
        _hours.value = hours?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    fun setNotes(notes: String?) {
        _notes.value = notes?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    private fun checkForChanges() {
        val currentVenue = _venue.value ?: return
        
        val hasChanges = currentVenue.name != _venueName.value.trim() ||
                currentVenue.address != _address.value ||
                currentVenue.roomType != _roomType.value ||
                currentVenue.cost != _cost.value ||
                currentVenue.hours != _hours.value ||
                currentVenue.notes != _notes.value
        
        _hasUnsavedChanges.value = hasChanges
    }
    
    fun saveVenue() {
        if (_isSaving.value) return
        
        val currentVenue = _venue.value ?: return
        val name = _venueName.value.trim()
        
        if (name.isBlank()) {
            _saveResult.value = false
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val updatedVenue = currentVenue.copy(
                    name = name,
                    address = _address.value,
                    roomType = _roomType.value,
                    cost = _cost.value,
                    hours = _hours.value,
                    notes = _notes.value
                )
                
                repository.updateVenue(updatedVenue)
                _venue.value = updatedVenue
                _hasUnsavedChanges.value = false
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun deleteVenue() {
        val currentVenue = _venue.value ?: return
        
        viewModelScope.launch {
            try {
                repository.deleteVenue(currentVenue)
                _deleteResult.value = true
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }
    
    fun clearSaveResult() {
        _saveResult.value = null
    }
    
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val venueId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditVenueViewModel::class.java)) {
                return EditVenueViewModel(repository, venueId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}