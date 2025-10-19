package com.vs18.tvdemoapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs18.tvdemoapp.core.model.Movie
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PlaybackVideoFragmentTest {

    private lateinit var fragment: PlaybackVideoFragment
    private lateinit var activity: FragmentActivity
    private var mockContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    private val movie = Movie(
        id = 1,
        title = "Test Movie",
        videoUrl = "https://example.com/video.mp4",
        subtitleUrl = "https://example.com/subtitles.vtt",
        cardImageUrl = "https://example.com/image.jpg"
    )

    @Before
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics

        val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
        val networkCapabilities = mockk<NetworkCapabilities>(relaxed = true)
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { connectivityManager.activeNetwork } returns mockk()
        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities

        mockContext = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        activity = controller.get()

        // ⚡ Без spyk — звичайний інстанс
        fragment = PlaybackVideoFragment.newInstance(movie, false)

        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun playbackVideoFragment_playsVideo_whenValidMovieAndOnline() {
        fragment.onCreateView(LayoutInflater.from(activity), null, null)
        verify { mockCrashlytics.log("Playing video: Test Movie") }
    }

    @Test
    fun playbackVideoFragment_handlesMissingMovie() {
        val fragmentWithoutMovie = PlaybackVideoFragment.newInstance(null, false)

        activity.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragmentWithoutMovie)
            .commitNow()

        verify { mockCrashlytics.log("No movie data in PlaybackVideoFragment") }
    }

    @Test
    fun onCreateView_setsUpVideoScreenContent() {
        val inflater = LayoutInflater.from(activity)
        val container = mockk<ViewGroup>(relaxed = true)

        val view = fragment.onCreateView(inflater, container, null)
        assertNotNull(view)
        assertTrue(view is ComposeView)
    }
}
