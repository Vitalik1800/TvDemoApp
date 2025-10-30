package com.vs18.tvdemoapp

import android.content.*
import android.os.Build
import androidx.test.core.app.*
import com.vs18.tvdemoapp.security.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.*

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class SecurePrefsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        SecurePrefs.init(context)
    }

    @Test
    fun `SecurePrefs saves and reads token` () {
        SecurePrefs.userToken = "test123"
        assertEquals("test123", SecurePrefs.userToken)
    }

}