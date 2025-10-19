package com.vs18.tvdemoapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.vs18.tvdemoapp.core.db.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.core.repository.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.test.*

@ExperimentalCoroutinesApi
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class MovieRepositoryTest {

    private val movieDao = mockk<MovieDao>()
    private val context = mockk<Context>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private lateinit var repository: MovieRepository

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        coEvery { movieDao.getAll() } returns flowOf(emptyList())
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        repository = MovieRepository(movieDao, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMoviesFromNetwork emits movies and caches them when network available`() = runTest {
        // Налаштування моків для мережі
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val movies = listOf(
            Movie(
                id = 1,
                title = "Big Buck Bunny",
                description = "Funny short animated film by Blender",
                backgroundImageUrl = "https://peach.blender.org/wp-content/uploads/title_anouncement.jpg?x11217",
                cardImageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                studio = "Blender",
                subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/main/bigbuckbunny_en.vtt"
            ),
            Movie(
                id = 2,
                title = "Elephant Dream",
                description = "First open movie made with Blender",
                backgroundImageUrl = "https://orange.blender.org/wp-content/themes/orange/images/blog/ED_PiP.jpg",
                cardImageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                studio = "Blender",
                subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/refs/heads/main/elephants_dream.vtt"
            ),
            Movie(
                id = 3,
                title = "Sintel (HLS)",
                description = "Open movie Sintel (HLS streaming)",
                backgroundImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQgAcWHEmuvReSPEJ0oKJihPX84xCKOh0IWnA&s",
                cardImageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                studio = "Blender",
                subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/refs/heads/main/sintel.vtt"
            ),
            Movie(
                id = 4,
                title = "Tears of Steel (DASH)",
                description = "Sci-fi short film by Blender",
                backgroundImageUrl = "https://mango.blender.org/wp-content/uploads/2013/05/01_thom_celia_bridge.jpg",
                cardImageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                studio = "Blender",
                subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/refs/heads/main/tears_of_steel.vvt"
            )
        )
        coEvery { movieDao.insertAll(any()) } returns Unit
        coEvery { movieDao.getAll() } returns flowOf(movies)

        val emittedMovies = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emittedMovies.add(it) }

        assertEquals(movies, emittedMovies.first())
        coVerify { movieDao.insertAll(movies) }
    }

    @Test
    fun `getMoviesFromNetwork falls back to Room on network failure`() = runTest {
        // Налаштування моків для відсутності мережі
        every { connectivityManager.activeNetwork } returns null

        val movies = listOf(
            Movie(id = 1, title = "Test Movie", videoUrl = "https://example.com")
        )
        coEvery { movieDao.getAll() } returns flowOf(movies)
        coEvery { movieDao.insertAll(any()) } throws IOException("Network error")

        val emittedMovies = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emittedMovies.add(it) }

        assertEquals(movies, emittedMovies.first())
    }

    @Test
    fun `getMoviesFromNetwork returns offline movies when no network`() = runTest {
        // Налаштування моків для відсутності мережі
        every { connectivityManager.activeNetwork } returns null
        coEvery { movieDao.getAll() } returns flowOf(emptyList())
        coEvery { movieDao.insertAll(any()) } returns Unit

        val expectedOfflineMovies = listOf(
            Movie(
                id = 1001,
                title = "Offline Movie 1: The Adventure",
                description = "A placeholder adventure movie for offline mode",
                backgroundImageUrl = "",
                cardImageUrl = "",
                videoUrl = "",
                studio = "Local Studio",
                subtitleUrl = ""
            ),
            Movie(
                id = 1002,
                title = "Offline Movie 2: The Mystery",
                description = "A placeholder mystery movie for offline mode",
                backgroundImageUrl = "",
                cardImageUrl = "",
                videoUrl = "",
                studio = "Local Studio",
                subtitleUrl = ""
            )
        )

        val emittedMovies = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emittedMovies.add(it) }

        assertEquals(expectedOfflineMovies, emittedMovies.first())
        coVerify { movieDao.insertAll(expectedOfflineMovies) }
    }
}
