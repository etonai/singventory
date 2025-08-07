package com.pseddev.singventory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "performances",
    foreignKeys = [
        ForeignKey(
            entity = Visit::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("visitId"), Index("songId")]
)
data class Performance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val visitId: Long,       // Must belong to a visit
    val songId: Long,
    val keyAdjustment: Int = 0,  // Steps up/down from venue key (+/-)
    val notes: String? = null,   // Performance-specific notes (optional, can be added later)
    val timestamp: Long          // When during the visit this performance occurred
)