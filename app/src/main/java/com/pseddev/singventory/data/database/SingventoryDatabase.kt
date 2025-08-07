package com.pseddev.singventory.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.pseddev.singventory.data.dao.*
import com.pseddev.singventory.data.entity.*

@Database(
    entities = [
        Song::class,
        Venue::class,
        Visit::class,
        Performance::class,
        SongVenueInfo::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SingventoryDatabase : RoomDatabase() {
    
    abstract fun songDao(): SongDao
    abstract fun venueDao(): VenueDao
    abstract fun visitDao(): VisitDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun songVenueInfoDao(): SongVenueInfoDao
    
    companion object {
        @Volatile
        private var INSTANCE: SingventoryDatabase? = null
        
        fun getDatabase(context: Context): SingventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SingventoryDatabase::class.java,
                    "singventory_database"
                )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}