package com.vs18.tvdemoapp

import android.content.*
import android.os.*
import android.view.*
import androidx.fragment.app.*
import androidx.test.core.app.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.*
import com.google.firebase.crashlytics.*
import io.mockk.*
import org.junit.*
import org.junit.runner.*
import org.robolectric.*
import org.robolectric.annotation.*
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PlaybackVideoFragmentTest {

    private lateinit var fragment: PlaybackVideoFragment
    private val mockActivity = mockk<FragmentActivity>()
    private val mockIntent = mockk<Intent>()
    private val mockPlayer = mockk<SimpleExoPlayer>(relaxed = true)
    private val mockPlayerView = mockk<PlayerView>(relaxed = true)
    private val mockLayoutInflater = mockk<LayoutInflater>()
    private val mockContainer = mockk<ViewGroup>()
    private val mockCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    private val movie: Movie = Movie(
        id = 1,
        title = "Test Movie",
        videoUrl = "https://example.com/video.mp4",
        subtitleUrl = "https://example.com/subtitles.vtt"
    )

    @Before
    fun setup() {
        fragment = spyk(PlaybackVideoFragment(), recordPrivateCalls = true)

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics
        every { mockCrashlytics.log(any()) } just Runs

        mockkConstructor(SimpleExoPlayer.Builder::class)
        every { anyConstructed<SimpleExoPlayer.Builder>().build() } returns mockPlayer

        every { mockActivity.intent } returns mockIntent
        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns movie
        every { fragment.activity } returns mockActivity

        every { mockLayoutInflater.inflate(any<Int>(), any(), any()) } returns mockPlayerView
        every { mockPlayerView.context } returns mockActivity
        every { fragment.requireContext() } returns ApplicationProvider.getApplicationContext()
    }

    @Test
    fun onCreateView_initializePlayerView() {
        every { mockPlayerView.useController = any() } just Runs

        val view = fragment.onCreateView(mockLayoutInflater, mockContainer, null)

        verify { mockPlayerView.useController = true }
        assertTrue(view == mockPlayerView)
    }

    @Test
    fun onViewCreated_initializesExoPlayerAndLogs() {
        every { mockPlayer.setMediaItem(any()) } just Runs
        every { mockPlayer.prepare() } just Runs
        every { mockPlayer.playWhenReady = any() } just Runs
        every { mockPlayerView.player = any() } just Runs

        every { fragment.activity } returns mockActivity
        every { mockActivity.intent } returns mockIntent
        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns movie

        fragment.onCreateView(mockLayoutInflater, mockContainer, null)
        fragment.onViewCreated(mockPlayerView, null)

        verify { anyConstructed<SimpleExoPlayer.Builder>().build() }
        verify { mockPlayerView.player = mockPlayer }
        verify { mockPlayer.setMediaItem(any()) }
        verify { mockPlayer.prepare() }
        verify { mockPlayer.playWhenReady = true }
        verify { mockCrashlytics.log("Playing video: Test Movie") }
    }

    @Test
    fun onViewCreated_handlesMissingVideoUrlGracefully_unit() {
        val movieNoUrl = Movie(id = 2, title = "No URL Movie", videoUrl = null, subtitleUrl = null)
        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns movieNoUrl

        fragment.onCreateView(mockLayoutInflater, mockContainer, null)
        fragment.onViewCreated(mockPlayerView, null)

        verify(exactly = 0) { mockPlayer.setMediaItem(any()) }
        verify(exactly = 0) { mockPlayerView.player = any() }
        verify(exactly = 0) { mockCrashlytics.log(any()) }
    }

    @Test
    fun onPause_pausesPlayer() {
        every { mockPlayer.setMediaItem(any()) } just Runs
        every { mockPlayer.prepare() } just Runs
        every { mockPlayer.playWhenReady = any() } just Runs
        every { mockPlayerView.player = any() } just Runs

        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns movie

        fragment.onCreateView(mockLayoutInflater, mockContainer, null)
        fragment.onViewCreated(mockPlayerView, null)
        fragment.onPause()

        verify { mockPlayer.pause() }
    }

    @Test
    fun onDestroy_releasesPlayer() {
        every { mockPlayerView.player = any() } just Runs
        every { mockPlayer.release() } just Runs

        every { mockIntent.getParcelableExtra<Movie>(DetailsActivity.MOVIE) } returns movie

        fragment.onCreateView(mockLayoutInflater, mockContainer, null)
        fragment.onViewCreated(mockPlayerView, null)

        fragment.onDestroy()

        verify { mockPlayer.release() }
    }

    @Test
    fun onCreateView_withLaunchFragmentInContainer() {
        val fragment = spyk<PlaybackVideoFragment>()

        val mockInflater = mockk<LayoutInflater>(relaxed = true)
        val mockContainer = mockk<ViewGroup>(relaxed = true)
        val mockBundle = Bundle().apply {
            putParcelable(DetailsActivity.MOVIE, movie)
        }

        val playerView = mockk<PlayerView>(relaxed = true)
        every { fragment.onCreateView(mockInflater, mockContainer, mockBundle) } returns playerView

        val view = fragment.onCreateView(mockInflater, mockContainer, mockBundle)

        assertTrue(view is PlayerView)
    }

    @Test
    fun onViewCreated_handlesMissingVideoUrlGracefully_integration() {
        val movie = Movie(id = 3, title = "Integration Movie", videoUrl = null, subtitleUrl = null)

        val fragment = spyk<PlaybackVideoFragment>()

        fragment.arguments = Bundle().apply {
            putParcelable(DetailsActivity.MOVIE, movie)
        }

        val mockPlayerView = mockk<PlayerView>(relaxed = true)

        every { fragment.onCreateView(any<LayoutInflater>(), any<ViewGroup>(), any()) } returns mockPlayerView

        fragment.onViewCreated(mockPlayerView, null)

        assertTrue(mockPlayerView is PlayerView)
    }
}