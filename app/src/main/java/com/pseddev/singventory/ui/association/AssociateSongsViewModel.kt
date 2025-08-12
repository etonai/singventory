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

data class AssociationFilter(
    val showUnassociated: Boolean = true,
    val showAssociated: Boolean = false,
    val showFrequentlyPerformed: Boolean = false
)

class AssociateSongsViewModel(
    private val repository: SingventoryRepository,
    private val venueId: Long
) : ViewModel() {

    private val _venue = MutableStateFlow<Venue?>(null)
    val venue: StateFlow<Venue?> = _venue.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(AssociationFilter())
    val filter: StateFlow<AssociationFilter> = _filter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItems: StateFlow<Set<Long>> = _selectedItems.asStateFlow()

    private val _associatedSongsCount = MutableStateFlow(0)
    val associatedSongsCount: StateFlow<Int> = _associatedSongsCount.asStateFlow()

    // Combine all data sources to create the filtered song association items
    val songAssociationItems = combine(
        repository.getAllSongs(),
        repository.getSongVenueInfoByVenue(venueId),
        _searchQuery,
        _filter,
        _selectedItems
    ) { allSongs, venueAssociations, query, filterSettings, selectedIds ->
        
        // Create a map of existing associations for quick lookup
        val associationMap = venueAssociations.associateBy { it.songId }
        
        // Filter songs based on search query
        val filteredSongs = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.name.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true)
            }
        }
        
        // Apply association filters and create items
        val items = filteredSongs.mapNotNull { song ->
            val association = associationMap[song.id]
            val isAssociated = association != null
            
            // Apply filter logic
            val shouldInclude = when {
                filterSettings.showUnassociated && !isAssociated -> true
                filterSettings.showAssociated && isAssociated -> true
                filterSettings.showFrequentlyPerformed && song.totalPerformances >= 3 -> true
                !filterSettings.showUnassociated && !filterSettings.showAssociated && !filterSettings.showFrequentlyPerformed -> true
                else -> false
            }
            
            if (shouldInclude) {
                SongAssociationItem(
                    song = song,
                    existingAssociation = association,
                    isSelected = selectedIds.contains(song.id)
                )
            } else {
                null
            }
        }.sortedWith(compareByDescending<SongAssociationItem> { it.song.totalPerformances }.thenBy { it.song.name })

        items
    }

    init {
        loadVenue()
        updateAssociatedSongsCount()
    }

    private fun loadVenue() {
        viewModelScope.launch {
            _venue.value = repository.getVenueById(venueId)
        }
    }

    private fun updateAssociatedSongsCount() {
        viewModelScope.launch {
            _associatedSongsCount.value = repository.getSongCountForVenue(venueId)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(newFilter: AssociationFilter) {
        _filter.value = newFilter
    }

    fun toggleSongSelection(songId: Long, isSelected: Boolean) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (isSelected) {
            currentSelection.add(songId)
        } else {
            currentSelection.remove(songId)
        }
        _selectedItems.value = currentSelection
    }

    fun clearSelections() {
        _selectedItems.value = emptySet()
    }

    suspend fun associateSongs(songIds: List<Long>): Boolean {
        return try {
            _isLoading.value = true
            songIds.forEach { songId ->
                // Check if association already exists
                val existing = repository.getSongVenueInfo(songId, venueId)
                if (existing == null) {
                    // Create new association with default values (keyAdjustment = -999 for Unknown)
                    val association = SongVenueInfo(
                        songId = songId,
                        venueId = venueId,
                        venuesSongId = null,
                        venueKey = null,
                        keyAdjustment = SongVenueInfo.UNKNOWN_KEY_ADJUSTMENT,
                        lyrics = null
                    )
                    repository.insertSongVenueInfo(association)
                }
            }
            updateAssociatedSongsCount()
            clearSelections()
            true
        } catch (e: Exception) {
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun associateSingleSong(songId: Long): Boolean {
        return associateSongs(listOf(songId))
    }

    suspend fun associateSongWithDetails(
        songId: Long,
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
            updateAssociatedSongsCount()
            true
        } catch (e: Exception) {
            false
        } finally {
            _isLoading.value = false
        }
    }

    class Factory(
        private val repository: SingventoryRepository,
        private val venueId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssociateSongsViewModel::class.java)) {
                return AssociateSongsViewModel(repository, venueId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}