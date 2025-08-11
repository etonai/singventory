package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.Performance
import kotlinx.coroutines.flow.Flow

data class PerformanceWithSongInfo(
    @Embedded val performance: Performance,
    val songName: String,
    val artistName: String
)

@Dao
interface PerformanceDao {
    
    @Query("SELECT * FROM performances ORDER BY timestamp DESC")
    fun getAllPerformances(): Flow<List<Performance>>
    
    @Query("SELECT * FROM performances WHERE id = :id")
    suspend fun getPerformanceById(id: Long): Performance?
    
    @Query("SELECT * FROM performances WHERE visitId = :visitId ORDER BY timestamp ASC")
    fun getPerformancesByVisit(visitId: Long): Flow<List<Performance>>
    
    @Query("""
        SELECT p.*, s.name as songName, s.artist as artistName
        FROM performances p 
        INNER JOIN songs s ON p.songId = s.id 
        WHERE p.visitId = :visitId 
        ORDER BY p.timestamp ASC
    """)
    fun getPerformancesByVisitWithSongInfo(visitId: Long): Flow<List<PerformanceWithSongInfo>>
    
    @Query("SELECT * FROM performances WHERE songId = :songId ORDER BY timestamp DESC")
    fun getPerformancesBySong(songId: Long): Flow<List<Performance>>
    
    @Query("""
        SELECT p.* FROM performances p 
        INNER JOIN visits v ON p.visitId = v.id 
        WHERE v.venueId = :venueId AND p.songId = :songId 
        ORDER BY p.timestamp DESC
    """)
    fun getPerformancesBySongAtVenue(songId: Long, venueId: Long): Flow<List<Performance>>
    
    @Query("SELECT * FROM performances WHERE visitId IN (:visitIds)")
    suspend fun getPerformancesByVisitIds(visitIds: List<Long>): List<Performance>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerformance(performance: Performance): Long
    
    @Update
    suspend fun updatePerformance(performance: Performance)
    
    @Delete
    suspend fun deletePerformance(performance: Performance)
    
    @Query("DELETE FROM performances WHERE visitId IN (:visitIds)")
    suspend fun deletePerformancesByVisitIds(visitIds: List<Long>)
    
    @Query("SELECT COUNT(*) FROM performances")
    suspend fun getPerformanceCount(): Int
    
    @Query("SELECT COUNT(*) FROM performances WHERE songId = :songId")
    suspend fun getPerformanceCountForSong(songId: Long): Int
    
    @Query("""
        SELECT COUNT(*) FROM performances p 
        INNER JOIN visits v ON p.visitId = v.id 
        WHERE v.venueId = :venueId AND p.songId = :songId
    """)
    suspend fun getPerformanceCountForSongAtVenue(songId: Long, venueId: Long): Int
    
    @Query("DELETE FROM performances")
    suspend fun deleteAllPerformances()
    
    @Query("SELECT * FROM performances WHERE songId = :songId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPerformanceForSong(songId: Long): Performance?
    
    @Query("""
        SELECT p.* FROM performances p 
        INNER JOIN visits v ON p.visitId = v.id 
        WHERE v.venueId = :venueId AND p.songId = :songId 
        ORDER BY p.timestamp DESC LIMIT 1
    """)
    suspend fun getLatestPerformanceForSongAtVenue(songId: Long, venueId: Long): Performance?
}