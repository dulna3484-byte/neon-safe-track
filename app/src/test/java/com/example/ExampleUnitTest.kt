package com.example

import com.example.data.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPasswordHashing() {
        val password = "MySecretPassword"
        val hash = SecurityHelper.hashPassword(password)
        assertNotEquals(password, hash)
        assertTrue(SecurityHelper.verifyPassword(password, hash))
        assertFalse(SecurityHelper.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testSharingCodeFormat() {
        val code = SecurityHelper.generateSharingCode()
        assertTrue(code.startsWith("SAFE-"))
        assertEquals(9, code.length)
    }
}
