package com.pseddev.singventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val artist: String,
    val referenceKey: String?,
    val preferredKey: String?,
    val lyrics: String?,
    
    // Calculated fields - preserved during purging
    val totalPerformances: Int = 0,
    val lastPerformed: Long? = null  // Timestamp - frozen at actual last performance even after purging
)