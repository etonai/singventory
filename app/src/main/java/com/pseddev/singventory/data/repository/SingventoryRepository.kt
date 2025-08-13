package com.pseddev.singventory.data.repository

import com.pseddev.singventory.data.dao.*
import com.pseddev.singventory.data.dao.VisitWithDetailsEntity
import com.pseddev.singventory.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    
    fun getAllVisitsWithDetails() = visitDao.getAllVisitsWithDetails()
    
    suspend fun getVisitById(id: Long): Visit? = visitDao.getVisitById(id)
    
    fun getVisitsByVenue(venueId: Long): Flow<List<Visit>> = visitDao.getVisitsByVenue(venueId)
    
    suspend fun getActiveVisits(): List<Visit> = visitDao.getActiveVisits()
    
    suspend fun getActiveVisitForVenue(venueId: Long): Visit? = visitDao.getActiveVisitForVenue(venueId)
    
    fun getVisitsBetweenDates(startTime: Long, endTime: Long): Flow<List<Visit>> =
        visitDao.getVisitsBetweenDates(startTime, endTime)
    
    suspend fun insertVisit(visit: Visit): Long = visitDao.insertVisit(visit)
    
    suspend fun updateVisit(visit: Visit) = visitDao.updateVisit(visit)
    
    suspend fun deleteVisit(visit: Visit) = visitDao.deleteVisit(visit)
    
    suspend fun endVisit(visitId: Long, endTimestamp: Long) = visitDao.endVisit(visitId, endTimestamp)
    
    suspend fun reopenVisit(visitId: Long) = visitDao.reopenVisit(visitId)
    
    suspend fun getVisitCount(): Int = visitDao.getVisitCount()
    
    suspend fun getVisitByVenueAndTimestamp(venueId: Long, timestamp: Long): Visit? = 
        visitDao.getVisitByVenueAndTimestamp(venueId, timestamp)
    
    // ================== PERFORMANCE OPERATIONS ==================
    
    fun getAllPerformances(): Flow<List<Performance>> = performanceDao.getAllPerformances()
    
    suspend fun getPerformanceById(id: Long): Performance? = performanceDao.getPerformanceById(id)
    
    fun getPerformancesByVisit(visitId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesByVisit(visitId)
        
    fun getPerformancesByVisitWithSongInfo(visitId: Long) =
        performanceDao.getPerformancesByVisitWithSongInfo(visitId)
    
    fun getPerformancesBySong(songId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesBySong(songId)
    
    fun getPerformancesBySongAtVenue(songId: Long, venueId: Long): Flow<List<Performance>> =
        performanceDao.getPerformancesBySongAtVenue(songId, venueId)
    
    suspend fun insertPerformance(performance: Performance): Long = performanceDao.insertPerformance(performance)
    
    suspend fun updatePerformance(performance: Performance) = performanceDao.updatePerformance(performance)
    
    suspend fun deletePerformance(performance: Performance) = performanceDao.deletePerformance(performance)
    
    suspend fun getPerformanceCount(): Int = performanceDao.getPerformanceCount()
    
    suspend fun getPerformanceCountForSong(songId: Long): Int = performanceDao.getPerformanceCountForSong(songId)
    
    suspend fun getPerformanceCountForSongAtVenue(songId: Long, venueId: Long): Int = 
        performanceDao.getPerformanceCountForSongAtVenue(songId, venueId)
    
    // ================== SONG-VENUE INFO OPERATIONS ==================
    
    fun getAllSongVenueInfo(): Flow<List<SongVenueInfo>> = songVenueInfoDao.getAllSongVenueInfo()
    
    suspend fun getSongVenueInfoById(id: Long): SongVenueInfo? = songVenueInfoDao.getSongVenueInfoById(id)
    
    fun getSongVenueInfoBySong(songId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongVenueInfoBySong(songId)
    
    fun getSongVenueInfoByVenue(venueId: Long): Flow<List<SongVenueInfo>> =
        songVenueInfoDao.getSongVenueInfoByVenue(venueId)
    
    fun getSongVenueInfoWithSongDetailsByVenue(venueId: Long) = 
        songVenueInfoDao.getSongVenueInfoWithSongDetailsByVenue(venueId)
    
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
     * Log a performance with statistics updates (overload for Performance object)
     */
    suspend fun logPerformance(performance: Performance): Long {
        return logPerformance(
            visitId = performance.visitId,
            songId = performance.songId,
            keyAdjustment = performance.keyAdjustment,
            notes = performance.notes,
            timestamp = performance.timestamp
        )
    }
    
    /**
     * Update a performance with proper statistics handling
     */
    suspend fun updatePerformanceWithStats(
        oldPerformance: Performance,
        newPerformance: Performance
    ) {
        // Get visit information for venue context
        val visit = getVisitById(newPerformance.visitId) ?: return
        
        // If song changed, handle statistics updates
        if (oldPerformance.songId != newPerformance.songId) {
            // Decrement stats for old song
            songDao.decrementPerformanceCount(oldPerformance.songId)
            
            // Decrement venue stats for old song
            val oldSongVenueInfo = getSongVenueInfo(oldPerformance.songId, visit.venueId)
            if (oldSongVenueInfo != null && oldSongVenueInfo.performanceCount > 0) {
                songVenueInfoDao.decrementVenuePerformanceCount(oldPerformance.songId, visit.venueId)
            }
            
            // Increment stats for new song
            songDao.incrementPerformanceCount(newPerformance.songId, newPerformance.timestamp)
            
            // Update or create song-venue info for new song
            val newSongVenueInfo = getSongVenueInfo(newPerformance.songId, visit.venueId)
            if (newSongVenueInfo != null) {
                songVenueInfoDao.incrementVenuePerformanceCount(newPerformance.songId, visit.venueId, newPerformance.timestamp)
            } else {
                // Create new song-venue relationship
                val songVenueInfo = SongVenueInfo(
                    songId = newPerformance.songId,
                    venueId = visit.venueId,
                    performanceCount = 1,
                    lastPerformed = newPerformance.timestamp
                )
                songVenueInfoDao.insertSongVenueInfo(songVenueInfo)
            }
        } else {
            // Same song, just update timestamp if it changed
            if (oldPerformance.timestamp != newPerformance.timestamp) {
                songDao.updateLastPerformed(newPerformance.songId, newPerformance.timestamp)
                songVenueInfoDao.updateLastPerformed(newPerformance.songId, visit.venueId, newPerformance.timestamp)
            }
        }
        
        // Update the performance record
        performanceDao.updatePerformance(newPerformance)
    }
    
    /**
     * Delete a performance with proper statistics handling
     */
    suspend fun deletePerformanceWithStats(performance: Performance) {
        // Get visit information for venue context
        val visit = getVisitById(performance.visitId) ?: return
        
        // Decrement song statistics
        songDao.decrementPerformanceCount(performance.songId)
        
        // Decrement venue statistics
        val songVenueInfo = getSongVenueInfo(performance.songId, visit.venueId)
        if (songVenueInfo != null && songVenueInfo.performanceCount > 0) {
            songVenueInfoDao.decrementVenuePerformanceCount(performance.songId, visit.venueId)
        }
        
        // Delete the performance
        performanceDao.deletePerformance(performance)
    }
    
    /**
     * Delete a visit with proper statistics handling
     */
    suspend fun deleteVisitWithStats(visit: Visit) {
        // Get all performances for this visit BEFORE deleting anything
        val performances = performanceDao.getPerformancesByVisitIds(listOf(visit.id))
        
        // Update venue visit statistics only if this visit was counted (had performances)
        if (performances.isNotEmpty()) {
            venueDao.decrementVisitCount(visit.venueId)
        }
        
        // For each performance, update statistics as if we're deleting the performance
        for (performance in performances) {
            // Decrement song statistics - each performance decrements by 1
            songDao.decrementPerformanceCount(performance.songId)
            
            // Decrement venue statistics - each performance decrements by 1
            val songVenueInfo = getSongVenueInfo(performance.songId, visit.venueId)
            if (songVenueInfo != null && songVenueInfo.performanceCount > 0) {
                songVenueInfoDao.decrementVenuePerformanceCount(performance.songId, visit.venueId)
            }
        }
        
        // Collect unique songs and song-venue pairs for lastPerformed updates
        val songIds = mutableSetOf<Long>()
        val songVenuePairs = mutableSetOf<Pair<Long, Long>>()
        
        for (performance in performances) {
            songIds.add(performance.songId)
            songVenuePairs.add(Pair(performance.songId, visit.venueId))
        }
        
        // Delete the visit (this will cascade delete all performances due to foreign key constraints)
        visitDao.deleteVisit(visit)
        
        // Now recalculate last performed dates after deletion
        for (songId in songIds) {
            val latestPerformance = performanceDao.getLatestPerformanceForSong(songId)
            latestPerformance?.let { 
                songDao.updateLastPerformed(songId, it.timestamp) 
            }
        }
        
        for ((songId, venueId) in songVenuePairs) {
            val latestVenuePerformance = performanceDao.getLatestPerformanceForSongAtVenue(songId, venueId)
            latestVenuePerformance?.let { 
                songVenueInfoDao.updateLastPerformed(songId, venueId, it.timestamp) 
            }
        }
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
    
    /**
     * Clear all data - DEBUG BUILDS ONLY
     * Complete database reset for testing fresh user scenarios
     */
    suspend fun clearAllData() {
        // Clear all tables in proper order to respect foreign key constraints
        performanceDao.deleteAllPerformances()
        visitDao.deleteAllVisits()
        songVenueInfoDao.deleteAllSongVenueInfo()
        songDao.deleteAllSongs()
        venueDao.deleteAllVenues()
    }
    
    /**
     * TESTING HELPER: Migrate existing 0 key adjustments to Unknown (-999)
     * This updates all SongVenueInfo records where keyAdjustment = 0 to keyAdjustment = -999
     * Used for testing the Unknown vs Zero key adjustment feature
     */
    suspend fun migrateZeroKeyAdjustmentsToUnknown(): Int {
        val allSongVenueInfo = songVenueInfoDao.getAllSongVenueInfo().first()
        var migratedCount = 0
        
        allSongVenueInfo.forEach { songVenueInfo ->
            if (songVenueInfo.keyAdjustment == 0) {
                val updated = songVenueInfo.copy(keyAdjustment = SongVenueInfo.UNKNOWN_KEY_ADJUSTMENT)
                songVenueInfoDao.updateSongVenueInfo(updated)
                migratedCount++
            }
        }
        
        return migratedCount
    }
}