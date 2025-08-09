package com.pseddev.singventory.ui.association

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class VenueAssociationFilter(
    val showUnassociated: Boolean = true,
    val showAssociated: Boolean = false,
    val showFrequentlyVisited: Boolean = false
)

class AssociateVenuesViewModel(
    private val repository: SingventoryRepository,
    private val songId: Long
) : ViewModel() {

    private val _song = MutableStateFlow<Song?>(null)
    val song: StateFlow<Song?> = _song.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(VenueAssociationFilter())
    val filter: StateFlow<VenueAssociationFilter> = _filter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItems: StateFlow<Set<Long>> = _selectedItems.asStateFlow()

    private val _associatedVenuesCount = MutableStateFlow(0)
    val associatedVenuesCount: StateFlow<Int> = _associatedVenuesCount.asStateFlow()

    // Combine all data sources to create the filtered venue association items
    val venueAssociationItems = combine(
        repository.getAllVenues(),
        repository.getSongVenueInfoBySong(songId),
        _searchQuery,
        _filter,
        _selectedItems
    ) { allVenues, songAssociations, query, filterSettings, selectedIds ->
        
        // Create a map of existing associations for quick lookup
        val associationMap = songAssociations.associateBy { it.venueId }
        
        // Filter venues based on search query
        val filteredVenues = if (query.isBlank()) {
            allVenues
        } else {
            allVenues.filter { venue ->
                venue.name.contains(query, ignoreCase = true) ||
                venue.address?.contains(query, ignoreCase = true) == true
            }
        }
        
        // Apply association filters and create items
        val items = filteredVenues.mapNotNull { venue ->
            val association = associationMap[venue.id]
            val isAssociated = association != null
            
            // Apply filter logic
            val shouldInclude = when {
                filterSettings.showUnassociated && !isAssociated -> true
                filterSettings.showAssociated && isAssociated -> true
                filterSettings.showFrequentlyVisited && venue.totalVisits >= 3 -> true
                !filterSettings.showUnassociated && !filterSettings.showAssociated && !filterSettings.showFrequentlyVisited -> true
                else -> false
            }
            
            if (shouldInclude) {
                VenueAssociationItem(
                    venue = venue,
                    existingAssociation = association,
                    isSelected = selectedIds.contains(venue.id)
                )
            } else {
                null
            }
        }.sortedWith(compareByDescending<VenueAssociationItem> { it.venue.totalVisits }.thenBy { it.venue.name })

        items
    }

    init {
        loadSong()
        updateAssociatedVenuesCount()
    }

    private fun loadSong() {
        viewModelScope.launch {
            _song.value = repository.getSongById(songId)
        }
    }

    private fun updateAssociatedVenuesCount() {
        viewModelScope.launch {
            _associatedVenuesCount.value = repository.getVenueCountForSong(songId)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(newFilter: VenueAssociationFilter) {
        _filter.value = newFilter
    }

    fun toggleVenueSelection(venueId: Long, isSelected: Boolean) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (isSelected) {
            currentSelection.add(venueId)
        } else {
            currentSelection.remove(venueId)
        }
        _selectedItems.value = currentSelection
    }

    fun clearSelections() {
        _selectedItems.value = emptySet()
    }

    suspend fun associateVenues(venueIds: List<Long>): Boolean {
        return try {
            _isLoading.value = true
            venueIds.forEach { venueId ->
                // Check if association already exists
                val existing = repository.getSongVenueInfo(songId, venueId)
                if (existing == null) {
                    // Create new association with default values
                    val association = SongVenueInfo(
                        songId = songId,
                        venueId = venueId,
                        venuesSongId = null,
                        venueKey = null,
                        keyAdjustment = 0,
                        lyrics = null
                    )
                    repository.insertSongVenueInfo(association)
                }
            }
            updateAssociatedVenuesCount()
            clearSelections()
            true
        } catch (e: Exception) {
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun associateSingleVenue(venueId: Long): Boolean {
        return associateVenues(listOf(venueId))
    }

    suspend fun associateVenueWithDetails(
        venueId: Long,
        venueSongId: String?,
        venueKey: String?,
        keyAdjustment: Int
    ): Boolean {
        return try {
            _isLoading.value = true
            
            // Check if association already exists
            val existing = repository.getSongVenueInfo(songId, venueId)
            if (existing != null) {
                // Update existing association
                val updated = existing.copy(
                    venuesSongId = venueSongId,
                    venueKey = venueKey,
                    keyAdjustment = keyAdjustment
                )
                repository.updateSongVenueInfo(updated)
            } else {
                // Create new association
                val association = SongVenueInfo(
                    songId = songId,
                    venueId = venueId,
                    venuesSongId = venueSongId,
                    venueKey = venueKey,
                    keyAdjustment = keyAdjustment,
                    lyrics = null
                )
                repository.insertSongVenueInfo(association)
            }
            updateAssociatedVenuesCount()
            true
        } catch (e: Exception) {
            false
        } finally {
            _isLoading.value = false
        }
    }

    class Factory(
        private val repository: SingventoryRepository,
        private val songId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssociateVenuesViewModel::class.java)) {
                return AssociateVenuesViewModel(repository, songId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}