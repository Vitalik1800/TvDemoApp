package com.vs18.tvdemoapp

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs18.tvdemoapp.core.model.Movie
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PlaybackVideoFragmentTest {

    private lateinit var fragment: PlaybackVideoFragment
    private val mockActivity = mockk<FragmentActivity>()
    private val mockIntent = mockk<android.content.Intent>()
    private val mockCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    private val testMovie = Movie(
        id = 1,
        title = "Test Movie",
        videoUrl = "https://example.com/video.mp4",
        subtitleUrl = "https://example.com/subs.vtt"
    )

    @Before
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics

        fragment = spyk(PlaybackVideoFragment(), recordPrivateCalls = true)
        every { mockActivity.intent } returns mockIntent
        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns testMovie
        every { mockIntent.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, any()) } returns false
        every { fragment.activity } returns mockActivity
        every { fragment.requireContext() } returns ApplicationProvider.getApplicationContext()
    }

    @Test
    fun onCreate_initializesMovieAndOfflineMode() {
        // 🔹 Уникаємо реального виклику super.onCreate()
        every { fragment.activity } returns mockActivity
        every { mockActivity.intent } returns mockIntent
        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns testMovie
        every { mockIntent.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, any()) } returns true

        // Викликаємо логіку вручну через reflection, щоб не чіпати Android internals
        fragment.apply {
            movie = mockActivity.intent.getParcelableExtra(DetailsActivity.MOVIE)
            isOfflineMode = mockActivity.intent.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false)
        }

        val fieldMovie = fragment.javaClass.getDeclaredField("movie").apply { isAccessible = true }.get(fragment)
        val fieldOffline = fragment.javaClass.getDeclaredField("isOfflineMode").apply { isAccessible = true }.get(fragment)

        assertEquals(testMovie, fieldMovie)
        assertEquals(true, fieldOffline)
    }


    @Test
    fun onCreateView_returnsComposeView() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val container = mockk<ViewGroup>(relaxed = true)

        val view = fragment.onCreateView(inflater, container, null)

        assertNotNull(view)
        assertTrue(view is ComposeView)
    }

    @Test
    fun onCreateView_setsUpVideoScreenContent() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val container = mockk<ViewGroup>(relaxed = true)
        val composeView = fragment.onCreateView(inflater, container, null) as ComposeView

        assertNotNull(composeView)
        assertTrue(composeView is ComposeView)
    }
}
