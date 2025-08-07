package com.pseddev.singventory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "song_venue_info",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Venue::class,
            parentColumns = ["id"],
            childColumns = ["venueId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId"), Index("venueId"), Index(value = ["songId", "venueId"], unique = true)]
)
data class SongVenueInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val songId: Long,
    val venueId: Long,
    val venuesSongId: String? = null,    // The venue's internal song code/ID
    val venueKey: String? = null,        // Key that this song is in at this venue
    val keyAdjustment: Int = 0,          // Steps up/down from venue key that user prefers
    val lyrics: String? = null,          // Venue-specific lyrics override (defaults to song lyrics)
    
    // Venue-specific performance stats - preserved during purging
    val performanceCount: Int = 0,       // Times performed at this specific venue
    val lastPerformed: Long? = null      // Last performance at this venue - frozen even after purging
)