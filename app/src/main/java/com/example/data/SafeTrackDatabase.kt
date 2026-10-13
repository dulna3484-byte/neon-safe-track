package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        SharingSessionEntity::class,
        TrustedContactEntity::class,
        LocationUpdateEntity::class,
        LocationHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeTrackDatabase : RoomDatabase() {
    abstract fun dao(): SafeTrackDao

    companion object {
        @Volatile
        private var INSTANCE: SafeTrackDatabase? = null

        fun getInstance(context: Context): SafeTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeTrackDatabase::class.java,
                    "safetrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
