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
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}