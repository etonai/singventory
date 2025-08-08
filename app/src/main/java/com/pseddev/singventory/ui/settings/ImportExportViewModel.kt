package com.pseddev.singventory.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.singventory.data.entity.*
import com.pseddev.singventory.data.repository.SingventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

sealed class ImportExportState {
    object Idle : ImportExportState()
    data class Progress(val message: String) : ImportExportState()
    data class Success(val message: String) : ImportExportState()
    data class Error(val message: String) : ImportExportState()
}

data class BackupData(
    val exportDate: String,
    val version: String = "1.0",
    val songs: List<Song>,
    val venues: List<Venue>,
    val visits: List<Visit>,
    val performances: List<Performance>,
    val songVenueInfo: List<SongVenueInfo>
)

class ImportExportViewModel(
    private val repository: SingventoryRepository,
    private val context: Context
) : ViewModel() {
    
    private val _exportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val exportState: StateFlow<ImportExportState> = _exportState.asStateFlow()
    
    private val _importState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importState: StateFlow<ImportExportState> = _importState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun resetStates() {
        _exportState.value = ImportExportState.Idle
        _importState.value = ImportExportState.Idle
        _isLoading.value = false
    }
    
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _exportState.value = ImportExportState.Progress("Preparing data...")
            
            try {
                // Collect all data
                val songs = repository.getAllSongs().first()
                _exportState.value = ImportExportState.Progress("Exporting ${songs.size} songs...")
                
                val venues = repository.getAllVenues().first()
                _exportState.value = ImportExportState.Progress("Exporting ${venues.size} venues...")
                
                val visits = repository.getAllVisits().first()
                _exportState.value = ImportExportState.Progress("Exporting ${visits.size} visits...")
                
                val performances = repository.getAllPerformances().first()
                _exportState.value = ImportExportState.Progress("Exporting ${performances.size} performances...")
                
                val songVenueInfo = repository.getAllSongVenueInfo().first()
                _exportState.value = ImportExportState.Progress("Exporting ${songVenueInfo.size} associations...")
                
                // Create backup data structure
                val backupData = BackupData(
                    exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    songs = songs,
                    venues = venues,
                    visits = visits,
                    performances = performances,
                    songVenueInfo = songVenueInfo
                )
                
                // Convert to JSON
                _exportState.value = ImportExportState.Progress("Writing to file...")
                val jsonString = backupDataToJson(backupData)
                
                // Write to file
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                
                _exportState.value = ImportExportState.Success("Exported ${songs.size} songs, ${venues.size} venues, ${visits.size} visits, ${performances.size} performances")
                
            } catch (e: Exception) {
                _exportState.value = ImportExportState.Error(e.message ?: "Unknown export error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _importState.value = ImportExportState.Progress("Reading file...")
            
            try {
                // Read JSON from file
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: throw Exception("Could not read file")
                
                _importState.value = ImportExportState.Progress("Parsing backup data...")
                val backupData = jsonToBackupData(jsonString)
                
                // Validate backup data
                _importState.value = ImportExportState.Progress("Validating data...")
                validateBackupData(backupData)
                
                // Import in correct order to maintain referential integrity
                var importedSongs = 0
                var importedVenues = 0
                var importedVisits = 0
                var importedPerformances = 0
                var importedAssociations = 0
                
                // Create ID mapping for relationships
                val songIdMap = mutableMapOf<Long, Long>()
                val venueIdMap = mutableMapOf<Long, Long>()
                val visitIdMap = mutableMapOf<Long, Long>()
                
                // Import songs first
                _importState.value = ImportExportState.Progress("Importing songs...")
                for (song in backupData.songs) {
                    val existingSong = repository.getSongByNameAndArtist(song.name, song.artist)
                    if (existingSong == null) {
                        val newSong = song.copy(id = 0) // Let database assign new ID
                        val newId = repository.insertSong(newSong)
                        songIdMap[song.id] = newId
                        importedSongs++
                    } else {
                        songIdMap[song.id] = existingSong.id
                    }
                }
                
                // Import venues
                _importState.value = ImportExportState.Progress("Importing venues...")
                for (venue in backupData.venues) {
                    val existingVenue = repository.getVenueByName(venue.name)
                    if (existingVenue == null) {
                        val newVenue = venue.copy(id = 0) // Let database assign new ID
                        val newId = repository.insertVenue(newVenue)
                        venueIdMap[venue.id] = newId
                        importedVenues++
                    } else {
                        venueIdMap[venue.id] = existingVenue.id
                    }
                }
                
                // Import visits
                _importState.value = ImportExportState.Progress("Importing visits...")
                for (visit in backupData.visits) {
                    val newVenueId = venueIdMap[visit.venueId] ?: continue
                    
                    // Check if visit with same venue and timestamp already exists
                    val existingVisit = repository.getVisitByVenueAndTimestamp(newVenueId, visit.timestamp)
                    if (existingVisit == null) {
                        val newVisit = visit.copy(id = 0, venueId = newVenueId)
                        val newId = repository.insertVisit(newVisit)
                        visitIdMap[visit.id] = newId
                        importedVisits++
                    } else {
                        // Use existing visit ID for performance mapping
                        visitIdMap[visit.id] = existingVisit.id
                    }
                }
                
                // Import performances
                _importState.value = ImportExportState.Progress("Importing performances...")
                for (performance in backupData.performances) {
                    val newVisitId = visitIdMap[performance.visitId] ?: continue
                    val newSongId = songIdMap[performance.songId] ?: continue
                    val newPerformance = performance.copy(id = 0, visitId = newVisitId, songId = newSongId)
                    repository.insertPerformance(newPerformance)
                    importedPerformances++
                }
                
                // Import song-venue associations
                _importState.value = ImportExportState.Progress("Importing associations...")
                for (association in backupData.songVenueInfo) {
                    val newSongId = songIdMap[association.songId] ?: continue
                    val newVenueId = venueIdMap[association.venueId] ?: continue
                    
                    // Check if association already exists
                    val existingAssociation = repository.getSongVenueInfo(newSongId, newVenueId)
                    if (existingAssociation == null) {
                        val newAssociation = association.copy(id = 0, songId = newSongId, venueId = newVenueId)
                        repository.insertSongVenueInfo(newAssociation)
                        importedAssociations++
                    }
                }
                
                _importState.value = ImportExportState.Success("Imported $importedSongs songs, $importedVenues venues, $importedVisits visits, $importedPerformances performances, $importedAssociations associations")
                
            } catch (e: Exception) {
                _importState.value = ImportExportState.Error(e.message ?: "Unknown import error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun backupDataToJson(backupData: BackupData): String {
        val jsonObject = JSONObject().apply {
            put("exportDate", backupData.exportDate)
            put("version", backupData.version)
            
            // Songs array
            put("songs", JSONArray().apply {
                backupData.songs.forEach { song ->
                    put(JSONObject().apply {
                        put("id", song.id)
                        put("name", song.name)
                        put("artist", song.artist)
                        put("referenceKey", song.referenceKey)
                        put("preferredKey", song.preferredKey)
                        put("lyrics", song.lyrics)
                        put("totalPerformances", song.totalPerformances)
                        put("lastPerformed", song.lastPerformed)
                    })
                }
            })
            
            // Venues array
            put("venues", JSONArray().apply {
                backupData.venues.forEach { venue ->
                    put(JSONObject().apply {
                        put("id", venue.id)
                        put("name", venue.name)
                        put("address", venue.address)
                        put("roomType", venue.roomType)
                        put("cost", venue.cost)
                        put("hours", venue.hours)
                        put("notes", venue.notes)
                        put("totalVisits", venue.totalVisits)
                        put("lastVisited", venue.lastVisited)
                    })
                }
            })
            
            // Visits array
            put("visits", JSONArray().apply {
                backupData.visits.forEach { visit ->
                    put(JSONObject().apply {
                        put("id", visit.id)
                        put("venueId", visit.venueId)
                        put("timestamp", visit.timestamp)
                        put("endTimestamp", visit.endTimestamp)
                        put("isActive", visit.isActive)
                        put("amountSpent", visit.amountSpent)
                        put("notes", visit.notes)
                    })
                }
            })
            
            // Performances array
            put("performances", JSONArray().apply {
                backupData.performances.forEach { performance ->
                    put(JSONObject().apply {
                        put("id", performance.id)
                        put("visitId", performance.visitId)
                        put("songId", performance.songId)
                        put("keyAdjustment", performance.keyAdjustment)
                        put("timestamp", performance.timestamp)
                        put("notes", performance.notes)
                    })
                }
            })
            
            // Song-venue associations array
            put("songVenueInfo", JSONArray().apply {
                backupData.songVenueInfo.forEach { association ->
                    put(JSONObject().apply {
                        put("id", association.id)
                        put("songId", association.songId)
                        put("venueId", association.venueId)
                        put("venuesSongId", association.venuesSongId)
                        put("venueKey", association.venueKey)
                        put("keyAdjustment", association.keyAdjustment)
                        put("lyrics", association.lyrics)
                        put("performanceCount", association.performanceCount)
                        put("lastPerformed", association.lastPerformed)
                    })
                }
            })
        }
        
        return jsonObject.toString(2) // Pretty print with 2-space indentation
    }
    
    private fun jsonToBackupData(jsonString: String): BackupData {
        val jsonObject = JSONObject(jsonString)
        
        val songs = mutableListOf<Song>()
        val songsArray = jsonObject.getJSONArray("songs")
        for (i in 0 until songsArray.length()) {
            val songJson = songsArray.getJSONObject(i)
            songs.add(Song(
                id = songJson.getLong("id"),
                name = songJson.getString("name"),
                artist = songJson.getString("artist"),
                referenceKey = songJson.optString("referenceKey").takeIf { it.isNotEmpty() },
                preferredKey = songJson.optString("preferredKey").takeIf { it.isNotEmpty() },
                lyrics = songJson.optString("lyrics").takeIf { it.isNotEmpty() },
                totalPerformances = songJson.getInt("totalPerformances"),
                lastPerformed = songJson.optLong("lastPerformed").takeIf { it != 0L }
            ))
        }
        
        val venues = mutableListOf<Venue>()
        val venuesArray = jsonObject.getJSONArray("venues")
        for (i in 0 until venuesArray.length()) {
            val venueJson = venuesArray.getJSONObject(i)
            venues.add(Venue(
                id = venueJson.getLong("id"),
                name = venueJson.getString("name"),
                address = venueJson.optString("address").takeIf { it.isNotEmpty() },
                roomType = venueJson.optString("roomType").takeIf { it.isNotEmpty() },
                cost = venueJson.optString("cost").takeIf { it.isNotEmpty() },
                hours = venueJson.optString("hours").takeIf { it.isNotEmpty() },
                notes = venueJson.optString("notes").takeIf { it.isNotEmpty() },
                totalVisits = venueJson.getInt("totalVisits"),
                lastVisited = venueJson.optLong("lastVisited").takeIf { it != 0L }
            ))
        }
        
        val visits = mutableListOf<Visit>()
        val visitsArray = jsonObject.getJSONArray("visits")
        for (i in 0 until visitsArray.length()) {
            val visitJson = visitsArray.getJSONObject(i)
            visits.add(Visit(
                id = visitJson.getLong("id"),
                venueId = visitJson.getLong("venueId"),
                timestamp = visitJson.getLong("timestamp"),
                endTimestamp = visitJson.optLong("endTimestamp").takeIf { it != 0L },
                isActive = visitJson.getBoolean("isActive"),
                amountSpent = visitJson.optDouble("amountSpent").takeIf { !it.isNaN() },
                notes = visitJson.optString("notes").takeIf { it.isNotEmpty() }
            ))
        }
        
        val performances = mutableListOf<Performance>()
        val performancesArray = jsonObject.getJSONArray("performances")
        for (i in 0 until performancesArray.length()) {
            val performanceJson = performancesArray.getJSONObject(i)
            performances.add(Performance(
                id = performanceJson.getLong("id"),
                visitId = performanceJson.getLong("visitId"),
                songId = performanceJson.getLong("songId"),
                keyAdjustment = performanceJson.getInt("keyAdjustment"),
                timestamp = performanceJson.getLong("timestamp"),
                notes = performanceJson.optString("notes").takeIf { it.isNotEmpty() }
            ))
        }
        
        val songVenueInfo = mutableListOf<SongVenueInfo>()
        val associationsArray = jsonObject.getJSONArray("songVenueInfo")
        for (i in 0 until associationsArray.length()) {
            val associationJson = associationsArray.getJSONObject(i)
            songVenueInfo.add(SongVenueInfo(
                id = associationJson.getLong("id"),
                songId = associationJson.getLong("songId"),
                venueId = associationJson.getLong("venueId"),
                venuesSongId = associationJson.optString("venuesSongId").takeIf { it.isNotEmpty() },
                venueKey = associationJson.optString("venueKey").takeIf { it.isNotEmpty() },
                keyAdjustment = associationJson.getInt("keyAdjustment"),
                lyrics = associationJson.optString("lyrics").takeIf { it.isNotEmpty() },
                performanceCount = associationJson.getInt("performanceCount"),
                lastPerformed = associationJson.optLong("lastPerformed").takeIf { it != 0L }
            ))
        }
        
        return BackupData(
            exportDate = jsonObject.getString("exportDate"),
            version = jsonObject.optString("version", "1.0"),
            songs = songs,
            venues = venues,
            visits = visits,
            performances = performances,
            songVenueInfo = songVenueInfo
        )
    }
    
    private fun validateBackupData(backupData: BackupData) {
        // Basic validation
        if (backupData.songs.isEmpty() && backupData.venues.isEmpty() && 
            backupData.visits.isEmpty() && backupData.performances.isEmpty()) {
            throw Exception("Backup file appears to be empty or invalid")
        }
        
        // Validate referential integrity
        val songIds = backupData.songs.map { it.id }.toSet()
        val venueIds = backupData.venues.map { it.id }.toSet()
        val visitIds = backupData.visits.map { it.id }.toSet()
        
        // Check visits reference valid venues
        for (visit in backupData.visits) {
            if (visit.venueId !in venueIds) {
                throw Exception("Invalid backup: Visit references non-existent venue")
            }
        }
        
        // Check performances reference valid visits and songs
        for (performance in backupData.performances) {
            if (performance.visitId !in visitIds) {
                throw Exception("Invalid backup: Performance references non-existent visit")
            }
            if (performance.songId !in songIds) {
                throw Exception("Invalid backup: Performance references non-existent song")
            }
        }
        
        // Check associations reference valid songs and venues
        for (association in backupData.songVenueInfo) {
            if (association.songId !in songIds) {
                throw Exception("Invalid backup: Association references non-existent song")
            }
            if (association.venueId !in venueIds) {
                throw Exception("Invalid backup: Association references non-existent venue")
            }
        }
    }
    
    class Factory(
        private val repository: SingventoryRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ImportExportViewModel::class.java)) {
                return ImportExportViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}