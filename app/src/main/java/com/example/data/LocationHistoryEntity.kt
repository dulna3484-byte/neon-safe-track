package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 5.0f,
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)
