package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SafeTrack", appName)
    }

    @Test
    fun `security helper hashes password safely and generates code`() {
        val rawPassword = "SecurePassword123"
        val hash1 = SecurityHelper.hashPassword(rawPassword)
        val hash2 = SecurityHelper.hashPassword(rawPassword)

        assertEquals(hash1, hash2)
        assertNotEquals(rawPassword, hash1)
        assertTrue(SecurityHelper.verifyPassword(rawPassword, hash1))

        val code = SecurityHelper.generateSharingCode()
        assertTrue(code.startsWith("SAFE-"))
        assertEquals(9, code.length)
    }
}
