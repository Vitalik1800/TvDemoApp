package com.vs18.tvdemoapp

import android.content.*
import android.os.*
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.core.repository.*
import io.mockk.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.runner.*
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VideoDetailsFragmentTest {

    private lateinit var fragment: VideoDetailsFragment
    private val mockCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
    private val mockRepository = mockk<MovieRepository>()
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
        every { mockRepository.getMoviesFromNetwork() } returns flowOf(
            listOf(
                Movie(2, "Related Movie", null, null, "https://example.com/related.jpg")
            )
        )

        val activityController = Robolectric.buildActivity(DetailsActivity::class.java, Intent().apply {
            putExtra(DetailsActivity.MOVIE, movie)
            putExtra(DetailsActivity.IS_OFFLINE_MODE, false)
        })
        val activity = activityController.create().start().resume().get()
        fragment = VideoDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DetailsActivity.MOVIE, movie)
                putBoolean(DetailsActivity.IS_OFFLINE_MODE, false)
            }
        }
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "VideoDetailsFragment")
            .commitNow()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun videoDetailsFragment_displayDetails_whenValidMovie() {
        fragment.onCreateView(fragment.layoutInflater, null, null)

        verify { mockCrashlytics.log("Opened details for movie: ${movie.title}") }
    }

    @Test
    fun videoDetailsFragment_handlesMissingMovie() {
        val activityController = Robolectric.buildActivity(DetailsActivity::class.java, Intent()) // без extras
        val activity = activityController.create().start().resume().get()
        val fragment = VideoDetailsFragment()

        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "VideoDetailsFragment")
            .commitNow()

        verify { mockCrashlytics.log("No movie data provided in VideoDetailsScreen") }
    }

}