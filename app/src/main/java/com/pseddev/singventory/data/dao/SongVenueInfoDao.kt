package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.SongVenueInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface SongVenueInfoDao {
    
    @Query("SELECT * FROM song_venue_info")
    fun getAllSongVenueInfo(): Flow<List<SongVenueInfo>>
    
    @Query("SELECT * FROM song_venue_info WHERE id = :id")
    suspend fun getSongVenueInfoById(id: Long): SongVenueInfo?
    
    @Query("SELECT * FROM song_venue_info WHERE songId = :songId")
    fun getSongVenueInfoBySong(songId: Long): Flow<List<SongVenueInfo>>
    
    @Query("SELECT * FROM song_venue_info WHERE venueId = :venueId")
    fun getSongVenueInfoByVenue(venueId: Long): Flow<List<SongVenueInfo>>
    
    @Query("SELECT * FROM song_venue_info WHERE songId = :songId AND venueId = :venueId")
    suspend fun getSongVenueInfo(songId: Long, venueId: Long): SongVenueInfo?
    
    @Query("""
        SELECT svi.* FROM song_venue_info svi 
        INNER JOIN songs s ON svi.songId = s.id 
        WHERE svi.venueId = :venueId 
        ORDER BY s.totalPerformances DESC, svi.performanceCount DESC
    """)
    fun getSongsAtVenueByFrequency(venueId: Long): Flow<List<SongVenueInfo>>
    
    @Query("""
        SELECT svi.* FROM song_venue_info svi 
        INNER JOIN songs s ON svi.songId = s.id 
        WHERE svi.venueId = :venueId AND svi.performanceCount = 0
        ORDER BY s.totalPerformances DESC
    """)
    fun getUnperformedSongsAtVenue(venueId: Long): Flow<List<SongVenueInfo>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSongVenueInfo(songVenueInfo: SongVenueInfo): Long
    
    @Update
    suspend fun updateSongVenueInfo(songVenueInfo: SongVenueInfo)
    
    @Delete
    suspend fun deleteSongVenueInfo(songVenueInfo: SongVenueInfo)
    
    @Query("DELETE FROM song_venue_info WHERE songId = :songId")
    suspend fun deleteAllSongVenueInfoBySong(songId: Long)
    
    @Query("DELETE FROM song_venue_info WHERE venueId = :venueId")
    suspend fun deleteAllSongVenueInfoByVenue(venueId: Long)
    
    @Query("UPDATE song_venue_info SET performanceCount = performanceCount + 1, lastPerformed = :timestamp WHERE songId = :songId AND venueId = :venueId")
    suspend fun incrementVenuePerformanceCount(songId: Long, venueId: Long, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM song_venue_info WHERE songId = :songId")
    suspend fun getVenueCountForSong(songId: Long): Int
    
    @Query("SELECT COUNT(*) FROM song_venue_info WHERE venueId = :venueId")
    suspend fun getSongCountForVenue(venueId: Long): Int
}