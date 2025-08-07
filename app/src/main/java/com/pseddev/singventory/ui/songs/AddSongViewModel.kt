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

class AddSongViewModel(private val repository: SingventoryRepository) : ViewModel() {
    
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
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()
    
    fun setSongName(name: String) {
        _songName.value = name.trim()
    }
    
    fun setArtist(artist: String) {
        _artist.value = artist.trim()
    }
    
    fun setReferenceKey(key: MusicalKey?) {
        _referenceKey.value = key
    }
    
    fun setPreferredKey(key: MusicalKey?) {
        _preferredKey.value = key
    }
    
    fun setLyrics(lyrics: String?) {
        _lyrics.value = lyrics?.trim()?.takeIf { it.isNotBlank() }
    }
    
    fun saveSong() {
        if (_isSaving.value) return
        
        val name = _songName.value.trim()
        val artistName = _artist.value.trim().takeIf { it.isNotBlank() }
        
        if (name.isBlank()) {
            _saveResult.value = false
            return
        }
        
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val song = Song(
                    name = name,
                    artist = artistName ?: "",
                    referenceKey = _referenceKey.value?.displayName,
                    preferredKey = _preferredKey.value?.displayName,
                    lyrics = _lyrics.value
                )
                
                repository.insertSong(song)
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
            if (modelClass.isAssignableFrom(AddSongViewModel::class.java)) {
                return AddSongViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}