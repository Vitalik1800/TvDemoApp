package com.vs18.tvdemoapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.vs18.tvdemoapp.core.db.MovieDao
import com.vs18.tvdemoapp.core.model.Movie
import com.vs18.tvdemoapp.core.repository.MovieRepository
import com.vs18.tvdemoapp.security.SecurePrefs
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Android 13
class MovieRepositoryTest {

    private val movieDao = mockk<MovieDao>()
    private val context = mockk<Context>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private lateinit var repository: MovieRepository

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockkObject(SecurePrefs)
        every { SecurePrefs.init(any()) } just Runs
        coEvery { movieDao.insertAll(any()) } just Runs
        coEvery { movieDao.getAll() } returns flowOf(emptyList())
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        repository = MovieRepository(movieDao, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(SecurePrefs)
    }

    @Test
    fun `returns online movies when network available`() = runTest {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val emitted = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emitted.add(it) }

        // Перевіримо, що щось справді повертається
        assert(emitted.first().isNotEmpty())
        coVerify { movieDao.insertAll(any()) }
    }

    @Test
    fun `returns offline movies when no network`() = runTest {
        every { connectivityManager.activeNetwork } returns null
        coEvery { movieDao.getAll() } returns flowOf(emptyList())

        val emitted = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emitted.add(it) }

        assertEquals(1001, emitted.first().first().id) // Offline Movie 1
        coVerify { movieDao.insertAll(any()) }
    }

    @Test
    fun getMoviesFromNetwork_returnsOfflineMovies_whenNoNetwork() = runTest {
        // Моки
        every { connectivityManager.activeNetwork } returns null
        coEvery { movieDao.insertAll(any()) } returns Unit

        // Виклик
        val emitted = mutableListOf<List<Movie>>()
        repository.getMoviesFromNetwork().collect { emitted.add(it) }

        // Очікування
        val expectedOffline = listOf(
            Movie(
                id = 1001,
                title = "Offline Movie 1: The Adventure",
                description = "A placeholder adventure movie for offline mode",
                backgroundImageUrl = "android.resource://com.vs18.tvdemoapp/drawable/offline_movie_1",
                cardImageUrl = "android.resource://com.vs18.tvdemoapp/drawable/offline_movie_1",
                videoUrl = "",
                studio = "Local Studio",
                subtitleUrl = ""
            ),
            Movie(
                id = 1002,
                title = "Offline Movie 2: The Mystery",
                description = "A placeholder mystery movie for offline mode",
                backgroundImageUrl = "android.resource://com.vs18.tvdemoapp/drawable/offline_movie_2",
                cardImageUrl = "android.resource://com.vs18.tvdemoapp/drawable/offline_movie_2",
                videoUrl = "",
                studio = "Local Studio",
                subtitleUrl = ""
            )
        )

        // Перевірки
        assertEquals(expectedOffline, emitted.first())
        coVerify { movieDao.insertAll(expectedOffline) }
    }

}
