package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeTrackDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUserDirect(): UserEntity?

    @Query("SELECT * FROM users WHERE emailOrPhone = :emailOrPhone LIMIT 1")
    suspend fun getUserByEmailOrPhone(emailOrPhone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logOutAllUsers()

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    // --- Sharing Sessions ---
    @Query("SELECT * FROM sharing_sessions WHERE ownerUserId = :userId AND sharingStatus = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
    fun getActiveSessionForUser(userId: Long): Flow<SharingSessionEntity?>

    @Query("SELECT * FROM sharing_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionByIdFlow(sessionId: String): Flow<SharingSessionEntity?>

    @Query("SELECT * FROM sharing_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionByIdDirect(sessionId: String): SharingSessionEntity?

    @Query("SELECT * FROM sharing_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<SharingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SharingSessionEntity)

    @Update
    suspend fun updateSession(session: SharingSessionEntity)

    @Query("UPDATE sharing_sessions SET sharingStatus = 'STOPPED' WHERE sessionId = :sessionId")
    suspend fun stopSession(sessionId: String)

    @Query("UPDATE sharing_sessions SET sharingStatus = 'STOPPED' WHERE ownerUserId = :userId")
    suspend fun stopAllSessionsForUser(userId: Long)

    // --- Trusted Contacts ---
    @Query("SELECT * FROM trusted_contacts WHERE userId = :userId ORDER BY name ASC")
    fun getContactsForUser(userId: Long): Flow<List<TrustedContactEntity>>

    @Query("SELECT * FROM trusted_contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: Long): TrustedContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity): Long

    @Update
    suspend fun updateContact(contact: TrustedContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: Long)

    // --- Location Updates ---
    @Query("SELECT * FROM location_updates WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getUpdatesForSession(sessionId: String): Flow<List<LocationUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationUpdate(update: LocationUpdateEntity): Long

    @Query("DELETE FROM location_updates WHERE sessionId = :sessionId")
    suspend fun deleteUpdatesForSession(sessionId: String)

    // --- Location History ---
    @Query("SELECT * FROM location_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLocationHistory(userId: Long): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationHistory(history: LocationHistoryEntity): Long

    @Query("DELETE FROM location_history WHERE id = :historyId")
    suspend fun deleteLocationHistoryEntry(historyId: Long)

    @Query("DELETE FROM location_history WHERE userId = :userId")
    suspend fun clearLocationHistory(userId: Long)
}
