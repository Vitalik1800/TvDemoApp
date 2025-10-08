package com.vs18.tvdemoapp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*

class MovieRepository(
    private val movieDao: MovieDao
) {

    fun getMoviesFromNetwork(): Flow<List<Movie>> = flow {
        delay(1000)
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
                backgroundImageUrl = "https://orange.blender.org/wp-content/themes/orange/images/common/ed_head.jpg",
                cardImageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                studio = "Blender",
                subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/refs/heads/main/elephants_dream.vtt"
            ),
            Movie(
                id = 3,
                title = "Sintel (HLS)",
                description = "Open movie Sintel (HLS streaming)",
                backgroundImageUrl = "https://durian.blender.org/wp-content/uploads/2010/05/sintel_poster.jpg",
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
        movieDao.insertAll(movies)
        emit(movies)
    }.catch { e ->
        if (e is Exception) {
            emitAll(movieDao.getAll())
        } else {
            throw e
        }
    }

    val movieState: StateFlow<List<Movie>> = movieDao.getAll()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _movieSelectionEvents = MutableSharedFlow<Movie>(replay = 0)
    val movieSelectionEvents: SharedFlow<Movie> = _movieSelectionEvents.asSharedFlow()

    suspend fun selectMovie(movie: Movie) {
        _movieSelectionEvents.emit(movie)
    }
}