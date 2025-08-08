package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.Visit
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    
    @Query("SELECT * FROM visits ORDER BY timestamp DESC")
    fun getAllVisits(): Flow<List<Visit>>
    
    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getVisitById(id: Long): Visit?
    
    @Query("SELECT * FROM visits WHERE venueId = :venueId ORDER BY timestamp DESC")
    fun getVisitsByVenue(venueId: Long): Flow<List<Visit>>
    
    @Query("SELECT * FROM visits WHERE isActive = 1")
    suspend fun getActiveVisits(): List<Visit>
    
    @Query("SELECT * FROM visits ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getOldestVisits(limit: Int): List<Visit>
    
    @Query("SELECT * FROM visits WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getVisitsBetweenDates(startTime: Long, endTime: Long): Flow<List<Visit>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVisit(visit: Visit): Long
    
    @Update
    suspend fun updateVisit(visit: Visit)
    
    @Delete
    suspend fun deleteVisit(visit: Visit)
    
    @Query("DELETE FROM visits WHERE id IN (:visitIds)")
    suspend fun deleteVisitsByIds(visitIds: List<Long>)
    
    @Query("UPDATE visits SET isActive = 0, endTimestamp = :endTimestamp WHERE id = :visitId")
    suspend fun endVisit(visitId: Long, endTimestamp: Long)
    
    @Query("UPDATE visits SET isActive = 1, endTimestamp = NULL WHERE id = :visitId")
    suspend fun reopenVisit(visitId: Long)
    
    @Query("SELECT COUNT(*) FROM visits")
    suspend fun getVisitCount(): Int
    
    @Query("SELECT COUNT(*) FROM visits WHERE venueId = :venueId")
    suspend fun getVisitCountForVenue(venueId: Long): Int
    
    @Query("SELECT * FROM visits WHERE venueId = :venueId AND timestamp = :timestamp LIMIT 1")
    suspend fun getVisitByVenueAndTimestamp(venueId: Long, timestamp: Long): Visit?
    
    @Query("DELETE FROM visits")
    suspend fun deleteAllVisits()
}