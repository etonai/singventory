package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    
    @Query("SELECT * FROM songs ORDER BY name ASC")
    fun getAllSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?
    
    @Query("SELECT * FROM songs WHERE name = :name AND artist = :artist")
    suspend fun getSongByNameAndArtist(name: String, artist: String): Song?
    
    @Query("SELECT * FROM songs ORDER BY lastPerformed DESC")
    fun getSongsByLastPerformed(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs ORDER BY totalPerformances DESC")
    fun getSongsByMostPerformed(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE name LIKE '%' || :searchTerm || '%' OR artist LIKE '%' || :searchTerm || '%' ORDER BY totalPerformances DESC")
    fun searchSongs(searchTerm: String): Flow<List<Song>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSong(song: Song): Long
    
    @Update
    suspend fun updateSong(song: Song)
    
    @Delete
    suspend fun deleteSong(song: Song)
    
    @Query("UPDATE songs SET totalPerformances = totalPerformances + 1, lastPerformed = :timestamp WHERE id = :songId")
    suspend fun incrementPerformanceCount(songId: Long, timestamp: Long)
    
    @Query("UPDATE songs SET totalPerformances = CASE WHEN totalPerformances > 0 THEN totalPerformances - 1 ELSE 0 END WHERE id = :songId")
    suspend fun decrementPerformanceCount(songId: Long)
    
    @Query("UPDATE songs SET lastPerformed = :timestamp WHERE id = :songId")
    suspend fun updateLastPerformed(songId: Long, timestamp: Long)
    
    @Query("UPDATE songs SET totalPerformances = 0, lastPerformed = NULL WHERE id = :songId")
    suspend fun resetPerformanceStats(songId: Long)
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}