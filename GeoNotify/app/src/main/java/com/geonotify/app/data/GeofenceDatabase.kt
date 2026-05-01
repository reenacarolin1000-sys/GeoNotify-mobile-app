package com.geonotify.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Incremented version to 2 because we added new fields to GeofenceEntity
@Database(entities = [GeofenceEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GeofenceDatabase : RoomDatabase() {
    abstract fun geofenceDao(): GeofenceDao

    companion object {
        @Volatile
        private var INSTANCE: GeofenceDatabase? = null

        fun getDatabase(context: Context): GeofenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeofenceDatabase::class.java,
                    "geofence_database"
                )
                .fallbackToDestructiveMigration() // This allows the app to update the table structure by clearing old data
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
