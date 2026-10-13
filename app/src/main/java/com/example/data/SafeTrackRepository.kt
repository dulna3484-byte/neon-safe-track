package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SafeTrackRepository(private val dao: SafeTrackDao) {

    val activeUserFlow: Flow<UserEntity?> = dao.getActiveUserFlow()

    fun getActiveSessionFlow(userId: Long): Flow<SharingSessionEntity?> {
        return dao.getActiveSessionForUser(userId)
    }

    fun getSessionByIdFlow(sessionId: String): Flow<SharingSessionEntity?> {
        return dao.getSessionByIdFlow(sessionId)
    }

    fun getContactsFlow(userId: Long): Flow<List<TrustedContactEntity>> {
        return dao.getContactsForUser(userId)
    }

    fun getLocationHistoryFlow(userId: Long): Flow<List<LocationHistoryEntity>> {
        return dao.getLocationHistory(userId)
    }

    suspend fun getActiveUserDirect(): UserEntity? {
        return dao.getActiveUserDirect()
    }

    suspend fun register(name: String, emailOrPhone: String, password: String): Result<UserEntity> {
        val existing = dao.getUserByEmailOrPhone(emailOrPhone.trim())
        if (existing != null) {
            return Result.failure(Exception("An account with this email/phone already exists."))
        }
        val passwordHash = SecurityHelper.hashPassword(password)
        // Log out any previously logged in session
        dao.logOutAllUsers()

        val newUser = UserEntity(
            name = name.trim(),
            emailOrPhone = emailOrPhone.trim(),
            passwordHash = passwordHash,
            isLoggedIn = true,
            locationHistoryEnabled = true,
            defaultDurationMinutes = 60
        )
        val newId = dao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)

        // Seed some starter trusted contacts for realistic experience
        dao.insertContact(
            TrustedContactEntity(
                userId = newId,
                name = "Emma Watson",
                phoneOrEmail = "emma.w@example.com",
                relationship = "Family",
                sharingStatus = "IDLE"
            )
        )
        dao.insertContact(
            TrustedContactEntity(
                userId = newId,
                name = "David Miller",
                phoneOrEmail = "+1 555-019-2834",
                relationship = "Friend",
                sharingStatus = "IDLE"
            )
        )

        return Result.success(createdUser)
    }

    suspend fun login(emailOrPhone: String, password: String): Result<UserEntity> {
        val user = dao.getUserByEmailOrPhone(emailOrPhone.trim())
            ?: return Result.failure(Exception("No account found with this email/phone. Please create an account."))

        if (!SecurityHelper.verifyPassword(password, user.passwordHash)) {
            return Result.failure(Exception("Invalid password. Please check your credentials."))
        }

        dao.logOutAllUsers()
        val loggedInUser = user.copy(isLoggedIn = true)
        dao.updateUser(loggedInUser)
        return Result.success(loggedInUser)
    }

    suspend fun logout() {
        dao.logOutAllUsers()
    }

    suspend fun deleteAccount(userId: Long) {
        dao.stopAllSessionsForUser(userId)
        dao.clearLocationHistory(userId)
        dao.deleteUserById(userId)
    }

    suspend fun startSharingSession(
        ownerUserId: Long,
        ownerName: String,
        invitedContactId: Long?,
        invitedName: String?,
        durationMinutes: Int, // e.g. 15, 60, 480, or -1 for "Until I stop"
        currentLatitude: Double,
        currentLongitude: Double,
        accuracy: Float,
        address: String
    ): SharingSessionEntity {
        // Stop any currently active session
        dao.stopAllSessionsForUser(ownerUserId)

        val code = SecurityHelper.generateSharingCode()
        val now = System.currentTimeMillis()
        val expiryTime = if (durationMinutes > 0) {
            now + (durationMinutes.toLong() * 60 * 1000)
        } else {
            Long.MAX_VALUE // Until I stop
        }

        val session = SharingSessionEntity(
            sessionId = code,
            ownerUserId = ownerUserId,
            ownerName = ownerName,
            invitedUserId = invitedContactId,
            invitedName = invitedName ?: "Anyone with code",
            startTime = now,
            expiryTime = expiryTime,
            sharingStatus = "ACTIVE",
            lastLatitude = currentLatitude,
            lastLongitude = currentLongitude,
            lastAccuracy = accuracy,
            lastUpdated = now,
            address = address
        )

        dao.insertSession(session)

        // If invited contact was selected, update contact sharing status
        if (invitedContactId != null) {
            val contact = dao.getContactById(invitedContactId)
            if (contact != null) {
                dao.updateContact(
                    contact.copy(
                        sharingStatus = "SHARING",
                        activeSessionId = code,
                        lastLocationUpdate = now,
                        lastLatitude = currentLatitude,
                        lastLongitude = currentLongitude,
                        lastAddress = address
                    )
                )
            }
        }

        // Add initial location update
        dao.insertLocationUpdate(
            LocationUpdateEntity(
                sessionId = code,
                latitude = currentLatitude,
                longitude = currentLongitude,
                accuracy = accuracy,
                timestamp = now,
                address = address
            )
        )

        return session
    }

    suspend fun stopSharing(sessionId: String, userId: Long) {
        dao.stopSession(sessionId)
        // Reset contacts sharing status
        val contacts = dao.getContactsForUser(userId).firstOrNull() ?: emptyList()
        for (c in contacts) {
            if (c.activeSessionId == sessionId || c.sharingStatus == "SHARING") {
                dao.updateContact(c.copy(sharingStatus = "IDLE", activeSessionId = null))
            }
        }
    }

    suspend fun updateLocationForActiveSession(
        sessionId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        address: String
    ) {
        val session = dao.getSessionByIdDirect(sessionId) ?: return
        if (session.sharingStatus != "ACTIVE") return

        // Check expiry
        if (System.currentTimeMillis() > session.expiryTime) {
            dao.updateSession(session.copy(sharingStatus = "EXPIRED"))
            return
        }

        val now = System.currentTimeMillis()
        dao.updateSession(
            session.copy(
                lastLatitude = latitude,
                lastLongitude = longitude,
                lastAccuracy = accuracy,
                lastUpdated = now,
                address = address
            )
        )

        dao.insertLocationUpdate(
            LocationUpdateEntity(
                sessionId = sessionId,
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                timestamp = now,
                address = address
            )
        )
    }

    suspend fun validateAndJoinSession(code: String): Result<SharingSessionEntity> {
        val trimmedCode = code.trim().uppercase()
        val session = dao.getSessionByIdDirect(trimmedCode)
            ?: return Result.failure(Exception("Invalid sharing code. Please verify the code with the person sharing."))

        if (session.sharingStatus == "STOPPED") {
            return Result.failure(Exception("This location sharing session was stopped by ${session.ownerName}."))
        }

        if (session.sharingStatus == "EXPIRED" || (session.expiryTime != Long.MAX_VALUE && System.currentTimeMillis() > session.expiryTime)) {
            dao.updateSession(session.copy(sharingStatus = "EXPIRED"))
            return Result.failure(Exception("This sharing code has expired."))
        }

        return Result.success(session)
    }

    suspend fun addContact(userId: Long, name: String, phoneOrEmail: String, relationship: String): Long {
        return dao.insertContact(
            TrustedContactEntity(
                userId = userId,
                name = name.trim(),
                phoneOrEmail = phoneOrEmail.trim(),
                relationship = relationship,
                sharingStatus = "IDLE"
            )
        )
    }

    suspend fun deleteContact(contactId: Long) {
        dao.deleteContact(contactId)
    }

    suspend fun recordLocationHistory(
        userId: Long,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        address: String
    ) {
        val user = dao.getActiveUserDirect()
        if (user != null && user.id == userId && user.locationHistoryEnabled) {
            dao.insertLocationHistory(
                LocationHistoryEntity(
                    userId = userId,
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = accuracy,
                    address = address,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteHistoryEntry(historyId: Long) {
        dao.deleteLocationHistoryEntry(historyId)
    }

    suspend fun clearHistory(userId: Long) {
        dao.clearLocationHistory(userId)
    }

    suspend fun toggleLocationHistory(userId: Long, enabled: Boolean) {
        val user = dao.getActiveUserDirect()
        if (user != null && user.id == userId) {
            dao.updateUser(user.copy(locationHistoryEnabled = enabled))
        }
    }

    suspend fun updateDefaultDuration(userId: Long, durationMinutes: Int) {
        val user = dao.getActiveUserDirect()
        if (user != null && user.id == userId) {
            dao.updateUser(user.copy(defaultDurationMinutes = durationMinutes))
        }
    }
}
