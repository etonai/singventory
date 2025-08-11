package com.pseddev.singventory.data.dao

import androidx.room.*
import com.pseddev.singventory.data.entity.SongVenueInfo
import kotlinx.coroutines.flow.Flow

// Data class for SongVenueInfo with Song details - flattened for Room compatibility
data class SongVenueInfoWithSongDetails(
    val id: Long,
    val songId: Long,
    val venueId: Long,
    val venuesSongId: String?,
    val venueKey: String?,
    val keyAdjustment: Int,
    val lyrics: String?,
    val performanceCount: Int,
    val lastPerformed: Long?,
    val songName: String,
    val artistName: String
)

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
    
    @Query("""
        SELECT 
            svi.*,
            s.name as songName,
            s.artist as artistName
        FROM song_venue_info svi
        INNER JOIN songs s ON svi.songId = s.id
        WHERE svi.venueId = :venueId
        ORDER BY s.name ASC
    """)
    fun getSongVenueInfoWithSongDetailsByVenue(venueId: Long): Flow<List<SongVenueInfoWithSongDetails>>
    
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
    
    @Query("UPDATE song_venue_info SET performanceCount = CASE WHEN performanceCount > 0 THEN performanceCount - 1 ELSE 0 END WHERE songId = :songId AND venueId = :venueId")
    suspend fun decrementVenuePerformanceCount(songId: Long, venueId: Long)
    
    @Query("UPDATE song_venue_info SET lastPerformed = :timestamp WHERE songId = :songId AND venueId = :venueId")
    suspend fun updateLastPerformed(songId: Long, venueId: Long, timestamp: Long)
    
    @Query("UPDATE song_venue_info SET performanceCount = 0, lastPerformed = NULL WHERE songId = :songId AND venueId = :venueId")
    suspend fun resetVenuePerformanceStats(songId: Long, venueId: Long)
    
    @Query("SELECT COUNT(*) FROM song_venue_info WHERE songId = :songId")
    suspend fun getVenueCountForSong(songId: Long): Int
    
    @Query("SELECT COUNT(*) FROM song_venue_info WHERE venueId = :venueId")
    suspend fun getSongCountForVenue(venueId: Long): Int
    
    @Query("DELETE FROM song_venue_info")
    suspend fun deleteAllSongVenueInfo()
}