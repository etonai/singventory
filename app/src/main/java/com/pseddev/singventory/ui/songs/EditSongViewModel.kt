package com.pseddev.singventory.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.MusicalKey
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditSongViewModel(
    private val repository: SingventoryRepository,
    private val songId: Long
) : ViewModel() {
    
    private val _song = MutableStateFlow<Song?>(null)
    val song: StateFlow<Song?> = _song.asStateFlow()
    
    private val _songName = MutableStateFlow("")
    val songName: StateFlow<String> = _songName.asStateFlow()
    
    private val _artist = MutableStateFlow("")
    val artist: StateFlow<String> = _artist.asStateFlow()
    
    private val _referenceKey = MutableStateFlow<MusicalKey?>(null)
    val referenceKey: StateFlow<MusicalKey?> = _referenceKey.asStateFlow()
    
    private val _preferredKey = MutableStateFlow<MusicalKey?>(null)
    val preferredKey: StateFlow<MusicalKey?> = _preferredKey.asStateFlow()
    
    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()
    
    private val _venuesAssociated = MutableStateFlow(0)
    val venuesAssociated: StateFlow<Int> = _venuesAssociated.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult.asStateFlow()
    
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()
    
    init {
        loadSong()
    }
    
    private fun loadSong() {
        viewModelScope.launch {
            try {
                val songData = repository.getSongById(songId)
                if (songData != null) {
                    _song.value = songData
                    _songName.value = songData.name
                    _artist.value = songData.artist
                    
                    // Convert string keys back to MusicalKey enum
                    _referenceKey.value = songData.referenceKey?.let { keyString ->
                        MusicalKey.entries.find { it.displayName == keyString }
                    }
                    _preferredKey.value = songData.preferredKey?.let { keyString ->
                        MusicalKey.entries.find { it.displayName == keyString }
                    }
                    
                    _lyrics.value = songData.lyrics
                    
                    // Load venue association count
                    _venuesAssociated.value = repository.getVenueCountForSong(songId)
                }
            } catch (e: Exception) {
                // Handle error - song not found
            }
        }
    }
    
    fun setSongName(name: String) {
        _songName.value = name.trim()
        checkForChanges()
    }
    
    fun setArtist(artist: String) {
        _artist.value = artist.trim()
        checkForChanges()
    }
    
    fun setReferenceKey(key: MusicalKey?) {
        _referenceKey.value = key
        checkForChanges()
    }
    
    fun setPreferredKey(key: MusicalKey?) {
        _preferredKey.value = key
        checkForChanges()
    }
    
    fun setLyrics(lyrics: String?) {
        _lyrics.value = lyrics?.trim()?.takeIf { it.isNotBlank() }
        checkForChanges()
    }
    
    private fun checkForChanges() {
        val currentSong = _song.value ?: return
        
        val hasChanges = currentSong.name != _songName.value.trim() ||
                currentSong.artist != _artist.value.trim() ||
                currentSong.referenceKey != _referenceKey.value?.displayName ||
                currentSong.preferredKey != _preferredKey.value?.displayName ||
                currentSong.lyrics != _lyrics.value
        
        _hasUnsavedChanges.value = hasChanges
    }
    
    fun saveSong() {
        if (_isSaving.value) return
        
        val currentSong = _song.value ?: return
        val name = _songName.value.trim()
        val artistName = _artist.value.trim().takeIf { it.isNotBlank() }
        
        if (name.isBlank()) {
            _saveResult.value = false
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val updatedSong = currentSong.copy(
                    name = name,
                    artist = artistName ?: "",
                    referenceKey = _referenceKey.value?.displayName,
                    preferredKey = _preferredKey.value?.displayName,
                    lyrics = _lyrics.value
                )
                
                repository.updateSong(updatedSong)
                _song.value = updatedSong
                _hasUnsavedChanges.value = false
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun deleteSong() {
        val currentSong = _song.value ?: return
        
        viewModelScope.launch {
            try {
                repository.deleteSong(currentSong)
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
        private val songId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditSongViewModel::class.java)) {
                return EditSongViewModel(repository, songId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}