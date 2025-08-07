package com.pseddev.singventory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = Venue::class,
            parentColumns = ["id"],
            childColumns = ["venueId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("venueId")]
)
data class Visit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val venueId: Long,
    val timestamp: Long,     // Start time of visit
    val endTimestamp: Long? = null,  // End time - null means visit is still active/can be reopened
    val notes: String?,
    val amountSpent: Double? = null,
    val isActive: Boolean = false    // True if visit is currently active/ongoing
)