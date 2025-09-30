package com.vs18.tvdemoapp

object MovieList {

    val MOVIE_CATEGORY = arrayOf(
        "Featured",
        "Popular",
        "New Releases"
    )

    val list = listOf(
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
}
