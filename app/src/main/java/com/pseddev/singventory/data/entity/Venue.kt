package com.pseddev.singventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venues")
data class Venue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val address: String?,
    val cost: String?,
    val roomType: String?, // "private", "public", "both"
    val hours: String?,
    val notes: String?,
    
    // Calculated fields - preserved during purging
    val totalVisits: Int = 0,
    val lastVisited: Long? = null  // Timestamp - frozen at actual last visit even after purging
)