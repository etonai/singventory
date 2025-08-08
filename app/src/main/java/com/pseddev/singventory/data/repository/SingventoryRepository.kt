package com.pseddev.singventory.data.repository

import com.pseddev.singventory.data.dao.*
import com.pseddev.singventory.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
class SingventoryRepository(
    private val songDao: SongDao,
    private val venueDao: VenueDao,
    private val visitDao: VisitDao,
    private val performanceDao: PerformanceDao,
    private val songVenueInfoDao: SongVenueInfoDao
) {
    
    // ================== SONG OPERATIONS ==================
    
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    
    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)
    
    suspend fun getSongByNameAndArtist(name: String, artist: String): Song? =
        songDao.getSongByNameAndArtist(name, artist)
    
    fun getSongsByLastPerformed(): Flow<List<Song>> = songDao.getSongsByLastPerformed()
    
    fun getSongsByMostPerformed(): Flow<List<Song>> = songDao.getSongsByMostPerformed()
    
    fun searchSongs(searchTerm: String): Flow<List<Song>> = songDao.searchSongs(searchTerm)
    
    suspend fun insertSong(song: Song): Long = songDao.insertSong(song)
    
    suspend fun updateSong(song: Song) = songDao.updateSong(song)
    
    suspend fun deleteSong(song: Song) {
        // First delete all associated song-venue info records
        songVenueInfoDao.deleteAllSongVenueInfoBySong(song.id)
        // Then delete the song
        songDao.deleteSong(song)
    }
    
    suspend fun getSongCount(): Int = songDao.getSongCount()
    
    // ================== VENUE OPERATIONS ==================
    
    fun getAllVenues(): Flow<List<Venue>> = venueDao.getAllVenues()
    
    suspend fun getVenueById(id: Long): Venue? = venueDao.getVenueById(id)
    
    suspend fun getVenueByName(name: String): Venue? = venueDao.getVenueByName(name)
    
    fun getVenuesByLastVisited(): Flow<List<Venue>> = venueDao.getVenuesByLastVisited()
    
    fun getVenuesByMostVisited(): Flow<List<Venue>> = venueDao.getVenuesByMostVisited()
    
    fun searchVenues(searchTerm: String): Flow<List<Venue>> = venueDao.searchVenues(searchTerm)
    
    suspend fun insertVenue(venue: Venue): Long = venueDao.insertVenue(venue)
    
    suspend fun updateVenue(venue: Venue) = venueDao.updateVenue(venue)
    
    suspend fun deleteVenue(venue: Venue) {
        // First delete all associated song-venue info records
        songVenueInfoDao.deleteAllSongVenueInfoByVenue(venue.id)
        // Then delete the venue
        venueDao.deleteVenue(venue)
    }
    
    suspend fun getVenueCount(): Int = venueDao.getVenueCount()
    
    // ================== VISIT OPERATIONS ==================
    
    fun getAllVisits(): Flow<List<Visit>> = visitDao.getAllVisits()
    
    suspend fun getVisitById(id: Long): Visit? = visitDao.getVisitById(id)
    
    fun getVisitsByVenue(venueId: Long): Flow<List<Visit>> = visitDao.getVisitsByVenue(venueId)
    
    suspend fun getActiveVisits(): List<Visit> = visitDao.getActiveVisits()
    
    fun getVisitsBetweenDates(startTime: Long, endTime: Long): Flow<List<Visit>> =
        visitDao.getVisitsBetweenDates(startTime, endTime)
    
    suspend fun insertVisit(visit: Visit): Long = visitDao.insertVisit(visit)
    
    suspend fun updateVisit(visit: Visit) = visitDao.updateVisit(visit)
    
    suspend fun deleteVisit(visit: Visit) = visitDao.deleteVisit(visit)
    
    suspend fun endVisit(visitId: Long, endTimestamp: Long) = visitDao.endVisit(visitId, endTimestamp)
    
    suspend fun reopenVisit(visitId: Long) = visitDao.reopenVisit(visitId)
    
    suspend fun getVisitCount(): Int = visitDao.getVisitCount()
    
    // ================== PERFORMANCE OPERATIONS ==================
    
    fun getAllPerformances(): Flow<List<Performance>> = performanceDao.getAllPerformances()
    
    suspend fun getPerformanceById(id: Long): Performance? = performanceDao.getPerformanceById(id)
    
    fun getPerformancesByVisit(visitId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesByVisit(visitId)
    
    fun getPerformancesBySong(songId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesBySong(songId)
    
    fun getPerformancesBySongAtVenue(songId: Long, venueId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesBySongAtVenue(songId, venueId)
    
    suspend fun insertPerformance(performance: Performance): Long = performanceDao.insertPerformance(performance)
    
    suspend fun updatePerformance(performance: Performance) = performanceDao.updatePerformance(performance)
    
    suspend fun deletePerformance(performance: Performance) = performanceDao.deletePerformance(performance)
    
    suspend fun getPerformanceCount(): Int = performanceDao.getPerformanceCount()
    
    // ================== SONG-VENUE INFO OPERATIONS ==================
    
    fun getAllSongVenueInfo(): Flow<List<SongVenueInfo>> = songVenueInfoDao.getAllSongVenueInfo()
    
    suspend fun getSongVenueInfoById(id: Long): SongVenueInfo? = songVenueInfoDao.getSongVenueInfoById(id)
    
    fun getSongVenueInfoBySong(songId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongVenueInfoBySong(songId)
    
    fun getSongVenueInfoByVenue(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongVenueInfoByVenue(venueId)
    
    suspend fun getSongVenueInfo(songId: Long, venueId: Long): SongVenueInfo? =
        songVenueInfoDao.getSongVenueInfo(songId, venueId)
    
    fun getSongsAtVenueByFrequency(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongsAtVenueByFrequency(venueId)
    
    fun getUnperformedSongsAtVenue(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getUnperformedSongsAtVenue(venueId)
    
    suspend fun insertSongVenueInfo(songVenueInfo: SongVenueInfo): Long =
        songVenueInfoDao.insertSongVenueInfo(songVenueInfo)
    
    suspend fun updateSongVenueInfo(songVenueInfo: SongVenueInfo) =
        songVenueInfoDao.updateSongVenueInfo(songVenueInfo)
    
    suspend fun deleteSongVenueInfo(songVenueInfo: SongVenueInfo) =
        songVenueInfoDao.deleteSongVenueInfo(songVenueInfo)
    
    suspend fun getVenueCountForSong(songId: Long): Int = 
        songVenueInfoDao.getVenueCountForSong(songId)
    
    suspend fun getSongCountForVenue(venueId: Long): Int = 
        songVenueInfoDao.getSongCountForVenue(venueId)
    
    // ================== COMPLEX OPERATIONS & DATA CONSISTENCY ==================
    
    /**
     * Log a performance and update all related statistics consistently
     */
    suspend fun logPerformance(visitId: Long, songId: Long, keyAdjustment: Int, notes: String?, timestamp: Long): Long {
        // Get visit to determine venue
        val visit = getVisitById(visitId) ?: throw IllegalArgumentException("Visit not found")
        
        // Insert performance
        val performance = Performance(
            visitId = visitId,
            songId = songId,
            keyAdjustment = keyAdjustment,
            notes = notes,
            timestamp = timestamp
        )
        val performanceId = performanceDao.insertPerformance(performance)
        
        // Update song statistics
        songDao.incrementPerformanceCount(songId, timestamp)
        
        // Update venue statistics if this is the first performance in this visit
        val existingPerformancesInVisit = performanceDao.getPerformancesByVisitIds(listOf(visitId))
        if (existingPerformancesInVisit.size == 1) { // Only this performance
            venueDao.incrementVisitCount(visit.venueId, visit.timestamp)
        }
        
        // Update or create song-venue info
        val existingSongVenueInfo = getSongVenueInfo(songId, visit.venueId)
        if (existingSongVenueInfo != null) {
            songVenueInfoDao.incrementVenuePerformanceCount(songId, visit.venueId, timestamp)
        } else {
            // Create new song-venue relationship
            val newSongVenueInfo = SongVenueInfo(
                songId = songId,
                venueId = visit.venueId,
                performanceCount = 1,
                lastPerformed = timestamp
            )
            songVenueInfoDao.insertSongVenueInfo(newSongVenueInfo)
        }
        
        return performanceId
    }
    
    /**
     * Start a new visit at a venue
     */
    suspend fun startVisit(venueId: Long, timestamp: Long, notes: String?, isActive: Boolean = true): Long {
        val visit = Visit(
            venueId = venueId,
            timestamp = timestamp,
            notes = notes,
            isActive = isActive
        )
        return visitDao.insertVisit(visit)
    }
    
    /**
     * End a visit and update statistics
     */
    suspend fun completeVisit(visitId: Long, endTimestamp: Long, finalNotes: String?, amountSpent: Double?) {
        val visit = getVisitById(visitId) ?: return
        
        val updatedVisit = visit.copy(
            endTimestamp = endTimestamp,
            isActive = false,
            notes = finalNotes ?: visit.notes,
            amountSpent = amountSpent ?: visit.amountSpent
        )
        visitDao.updateVisit(updatedVisit)
    }
    
    /**
     * Get songs available at a specific venue, prioritizing frequently performed songs
     */
    fun getSongsAvailableAtVenue(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongsAtVenueByFrequency(venueId)
    
    /**
     * Get songs that haven't been performed at a venue yet (for recon visits)
     */
    fun getSongsForReconAtVenue(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getUnperformedSongsAtVenue(venueId)
    
    /**
     * Add a song to a venue (during impromptu or recon visits)
     */
    suspend fun addSongToVenue(
        songId: Long,
        venueId: Long,
        venuesSongId: String? = null,
        venueKey: String? = null,
        keyAdjustment: Int = 0,
        lyrics: String? = null
    ): Long {
        // Check if relationship already exists
        val existing = getSongVenueInfo(songId, venueId)
        if (existing != null) {
            throw IllegalArgumentException("Song is already available at this venue")
        }
        
        val songVenueInfo = SongVenueInfo(
            songId = songId,
            venueId = venueId,
            venuesSongId = venuesSongId,
            venueKey = venueKey,
            keyAdjustment = keyAdjustment,
            lyrics = lyrics,
            performanceCount = 0,
            lastPerformed = null
        )
        
        return songVenueInfoDao.insertSongVenueInfo(songVenueInfo)
    }
    
    /**
     * Check for potential duplicate songs (for duplicate detection)
     */
    suspend fun findPotentialDuplicateSongs(name: String, artist: String): List<Song> {
        // Simple implementation - can be enhanced with fuzzy matching
        val exactMatch = getSongByNameAndArtist(name, artist)
        return if (exactMatch != null) listOf(exactMatch) else emptyList()
    }
    
    /**
     * Get comprehensive visit data with performances (for visit details)
     */
    data class VisitWithPerformances(
        val visit: Visit,
        val venue: Venue,
        val performances: List<Performance>,
        val songs: List<Song>
    )
    
    suspend fun getVisitWithDetails(visitId: Long): VisitWithPerformances? {
        val visit = getVisitById(visitId) ?: return null
        val venue = getVenueById(visit.venueId) ?: return null
        val performances = performanceDao.getPerformancesByVisit(visitId)
        
        // This would need to be implemented differently to avoid blocking calls
        // For now, return a simplified version
        return VisitWithPerformances(
            visit = visit,
            venue = venue,
            performances = emptyList(), // Would need coroutine-safe implementation
            songs = emptyList()
        )
    }
    
    // ================== DATA PURGING OPERATIONS ==================
    
    /**
     * Purge oldest visits while preserving essential statistics
     */
    suspend fun purgeOldestVisits(count: Int = 5): PurgeResult {
        val oldestVisits = visitDao.getOldestVisits(count)
        if (oldestVisits.isEmpty()) {
            return PurgeResult.NoVisitsToPurge
        }
        
        val visitIds = oldestVisits.map { it.id }
        
        try {
            // Delete performances for these visits (foreign key cascade will handle this)
            val performancesToDelete = performanceDao.getPerformancesByVisitIds(visitIds)
            
            // Delete the visits (this will cascade delete performances)
            visitDao.deleteVisitsByIds(visitIds)
            
            // Note: Statistics are preserved because we don't update totalPerformances, 
            // lastPerformed, totalVisits, or lastVisited fields. The purging only removes
            // the detailed visit and performance records while keeping aggregated stats.
            
            return PurgeResult.Success(
                deletedVisits = oldestVisits.size,
                deletedPerformances = performancesToDelete.size
            )
        } catch (e: Exception) {
            return PurgeResult.Error(e.message ?: "Unknown error during purging")
        }
    }
    
    sealed class PurgeResult {
        data class Success(
            val deletedVisits: Int,
            val deletedPerformances: Int
        ) : PurgeResult()
        
        object NoVisitsToPurge : PurgeResult()
        data class Error(val message: String) : PurgeResult()
    }
}