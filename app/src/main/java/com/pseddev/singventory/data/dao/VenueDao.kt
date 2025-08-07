package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.Venue
import kotlinx.coroutines.flow.Flow

@Dao
interface VenueDao {
    
    @Query("SELECT * FROM venues ORDER BY name ASC")
    fun getAllVenues(): Flow<List<Venue>>
    
    @Query("SELECT * FROM venues WHERE id = :id")
    suspend fun getVenueById(id: Long): Venue?
    
    @Query("SELECT * FROM venues WHERE name = :name")
    suspend fun getVenueByName(name: String): Venue?
    
    @Query("SELECT * FROM venues ORDER BY lastVisited DESC")
    fun getVenuesByLastVisited(): Flow<List<Venue>>
    
    @Query("SELECT * FROM venues ORDER BY totalVisits DESC")
    fun getVenuesByMostVisited(): Flow<List<Venue>>
    
    @Query("SELECT * FROM venues WHERE name LIKE '%' || :searchTerm || '%' OR address LIKE '%' || :searchTerm || '%' ORDER BY totalVisits DESC")
    fun searchVenues(searchTerm: String): Flow<List<Venue>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVenue(venue: Venue): Long
    
    @Update
    suspend fun updateVenue(venue: Venue)
    
    @Delete
    suspend fun deleteVenue(venue: Venue)
    
    @Query("UPDATE venues SET totalVisits = totalVisits + 1, lastVisited = :timestamp WHERE id = :venueId")
    suspend fun incrementVisitCount(venueId: Long, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM venues")
    suspend fun getVenueCount(): Int
}