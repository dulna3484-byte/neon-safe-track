package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_updates")
data class LocationUpdateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String? = null
)
