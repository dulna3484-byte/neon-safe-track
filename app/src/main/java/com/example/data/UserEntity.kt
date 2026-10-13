package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emailOrPhone: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false,
    val locationHistoryEnabled: Boolean = true,
    val defaultDurationMinutes: Int = 60
)
