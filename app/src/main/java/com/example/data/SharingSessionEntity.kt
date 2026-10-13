package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sharing_sessions")
data class SharingSessionEntity(
    @PrimaryKey
    val sessionId: String, // e.g. "SAFE-4821"
    val ownerUserId: Long,
    val ownerName: String,
    val invitedUserId: Long? = null,
    val invitedName: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val expiryTime: Long, // timestamp when sharing expires or Long.MAX_VALUE for Until I Stop
    val sharingStatus: String = "ACTIVE", // ACTIVE, STOPPED, EXPIRED
    val lastLatitude: Double = 37.7749,
    val lastLongitude: Double = -122.4194,
    val lastAccuracy: Float = 5.0f,
    val lastUpdated: Long = System.currentTimeMillis(),
    val address: String = "San Francisco, CA"
)
