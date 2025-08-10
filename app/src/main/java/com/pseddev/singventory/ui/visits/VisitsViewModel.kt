package com.pseddev.singventory.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Visit
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VisitsViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _activeVisit = MutableStateFlow<Visit?>(null)
    val activeVisit: StateFlow<Visit?> = _activeVisit.asStateFlow()
    
    // Get all visits with details (venue name and performance count)
    val visits = repository.getAllVisitsWithDetails().map { visitDetailsList ->
        visitDetailsList.map { visitDetails ->
            VisitWithDetails(
                visit = Visit(
                    id = visitDetails.id,
                    venueId = visitDetails.venueId,
                    timestamp = visitDetails.timestamp,
                    endTimestamp = visitDetails.endTimestamp,
                    isActive = visitDetails.isActive,
                    notes = visitDetails.notes,
                    amountSpent = visitDetails.amountSpent
                ),
                venueName = visitDetails.venueName ?: "Unknown Venue",
                performanceCount = visitDetails.performanceCount
            )
        }
    }
    
    init {
        // Load the most recent active visit if any
        viewModelScope.launch {
            val activeVisits = repository.getActiveVisits()
            if (activeVisits.isNotEmpty()) {
                _activeVisit.value = activeVisits.first()
            }
        }
    }
    
    suspend fun getVisitById(visitId: Long) = repository.getVisitById(visitId)
    
    fun endVisit(visitWithDetails: VisitWithDetails) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // End the visit by setting endTimestamp and isActive = false
                repository.endVisit(visitWithDetails.visit.id, System.currentTimeMillis())
                
                // Clear active visit if this was the active one
                if (_activeVisit.value?.id == visitWithDetails.visit.id) {
                    _activeVisit.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    
    fun deleteVisit(visitWithDetails: VisitWithDetails) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteVisitWithStats(visitWithDetails.visit)
                
                // Clear active visit if this was the active one
                if (_activeVisit.value?.id == visitWithDetails.visit.id) {
                    _activeVisit.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setActiveVisit(visitWithDetails: VisitWithDetails) {
        _activeVisit.value = visitWithDetails.visit
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VisitsViewModel::class.java)) {
                return VisitsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}