package com.example.data

import java.security.MessageDigest
import kotlin.random.Random

object SecurityHelper {
    private const val SALT = "SafeTrack_SecureSalt_v1"

    fun hashPassword(password: String): String {
        val input = "$SALT:$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }

    fun generateSharingCode(): String {
        // Generates code format: SAFE-XXXX (4 random alphanumeric characters)
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // avoids ambiguous chars 0/O, 1/I
        val randomSuffix = (1..4)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        return "SAFE-$randomSuffix"
    }
}
