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

class AddVenueViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
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
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()
    
    fun setVenueName(name: String) {
        _venueName.value = name.trim()
    }
    
    fun setAddress(address: String?) {
        _address.value = address?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun setRoomType(roomType: String?) {
        _roomType.value = roomType?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun setCost(cost: String?) {
        _cost.value = cost?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun setHours(hours: String?) {
        _hours.value = hours?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun setNotes(notes: String?) {
        _notes.value = notes?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun saveVenue() {
        if (_isSaving.value) return
        
        val name = _venueName.value.trim()
        
        if (name.isBlank()) {
            _saveResult.value = false
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val venue = Venue(
                    name = name,
                    address = _address.value,
                    cost = _cost.value,
                    roomType = _roomType.value,
                    hours = _hours.value,
                    notes = _notes.value
                )
                
                repository.insertVenue(venue)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun clearSaveResult() {
        _saveResult.value = null
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddVenueViewModel::class.java)) {
                return AddVenueViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}