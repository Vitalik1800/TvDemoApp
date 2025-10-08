package com.vs18.tvdemoapp

import android.net.*
import android.os.*
import com.google.android.exoplayer2.upstream.*
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.*
import kotlin.test.*

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class UnstableDataSourceTest {

    private val baseDataSource = mockk<DefaultHttpDataSource>(relaxed = true)
    private val dataspec = DataSpec(Uri.parse("https://example.com/video.mp4"))
    private lateinit var unstableDataSource: UnstableDataSource

    @Before
    fun setup() {
        unstableDataSource = UnstableDataSource(baseDataSource, delayMs = 0, failureProbability = 0.0f)
    }

    @Test
    fun `open delegates to baseDataSource when no failure` () {
        every { baseDataSource.open(dataspec) } returns 1024L
        val result = unstableDataSource.open(dataspec)
        assertEquals(1024L, result)
    }

    @Test
    fun `open throws HttpDataSourceException on failure`() {
        unstableDataSource = UnstableDataSource(baseDataSource, delayMs = 0, failureProbability = 1.0f)
        assertFailsWith<HttpDataSource.HttpDataSourceException> {
            unstableDataSource.open(dataspec)
        }
    }

    @Test
    fun `read limits data to random length`() {
        val buffer = ByteArray(1024)
        every { baseDataSource.read(buffer, 0, 1024) } returns 512
        val result = unstableDataSource.read(buffer, 0,1024)
        assert(result <= 1024)
    }

    @Test
    fun `close delegates to baseDataSource`() {
        every { baseDataSource.close() } returns Unit
        unstableDataSource.close()
        verify { baseDataSource.close() }
    }
}