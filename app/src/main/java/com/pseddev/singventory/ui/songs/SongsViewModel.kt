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
    
    // Combine all songs with search query to create filtered list
    val songs = combine(
        repository.getAllSongs(),
        _searchQuery
    ) { allSongs, query ->
        if (query.isBlank()) {
            // No search - sort by most performed (PlayStreak pattern)
            allSongs.sortedByDescending { it.totalPerformances }
        } else {
            // Search functionality prioritizing frequently performed songs
            allSongs.filter { song ->
                song.name.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true)
            }.sortedByDescending { it.totalPerformances }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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