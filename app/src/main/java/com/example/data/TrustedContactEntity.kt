package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val phoneOrEmail: String,
    val relationship: String = "Family", // Family, Friend, Partner, Emergency
    val sharingStatus: String = "IDLE", // SHARING, IDLE, PAUSED
    val activeSessionId: String? = null,
    val lastLocationUpdate: Long? = null,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val lastAddress: String? = null
)
