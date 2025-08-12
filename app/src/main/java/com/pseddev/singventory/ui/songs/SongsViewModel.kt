package com.pseddev.singventory.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SongsViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SongSortOption.TITLE)
    val sortOption: StateFlow<SongSortOption> = _sortOption.asStateFlow()
    
    private val _sortAscending = MutableStateFlow(true) // Default to ascending for title (A-Z)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()
    
    // Combine all songs with search query and sorting to create filtered and sorted list
    val songs = combine(
        repository.getAllSongs(),
        _searchQuery,
        _sortOption,
        _sortAscending
    ) { allSongs, query, sortOption, sortAscending ->
        val filteredSongs = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.name.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true)
            }
        }
        
        // Apply sorting
        val sortedSongs = when (sortOption) {
            SongSortOption.TITLE -> {
                if (sortAscending) filteredSongs.sortedBy { it.name.lowercase() }
                else filteredSongs.sortedByDescending { it.name.lowercase() }
            }
            SongSortOption.ARTIST -> {
                if (sortAscending) filteredSongs.sortedBy { it.artist.lowercase() }
                else filteredSongs.sortedByDescending { it.artist.lowercase() }
            }
            SongSortOption.PERFORMANCE_COUNT -> {
                if (sortAscending) filteredSongs.sortedBy { it.totalPerformances }
                else filteredSongs.sortedByDescending { it.totalPerformances }
            }
            SongSortOption.LAST_PERFORMANCE -> {
                if (sortAscending) filteredSongs.sortedBy { it.lastPerformed ?: 0L }
                else filteredSongs.sortedByDescending { it.lastPerformed ?: 0L }
            }
        }
        
        sortedSongs
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSortOption(sortOption: SongSortOption) {
        _sortOption.value = sortOption
        // Auto-adjust default sort order based on option
        _sortAscending.value = when (sortOption) {
            SongSortOption.TITLE, SongSortOption.ARTIST -> true // A-Z by default
            SongSortOption.PERFORMANCE_COUNT, SongSortOption.LAST_PERFORMANCE -> false // Most/Recent first by default
        }
    }
    
    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }
    
    suspend fun getSongById(songId: Long) = repository.getSongById(songId)
    
    fun deleteSong(song: Song) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSong(song)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    class Factory(private val repository: SingventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
                return SongsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}