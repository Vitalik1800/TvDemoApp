package com.vs18.tvdemoapp

import android.os.*
import android.view.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.leanback.app.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.*
import com.google.android.exoplayer2.util.*
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PlaybackVideoFragment : Fragment() {

    private var player: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Створюємо PlayerView
        playerView = PlayerView(requireContext()).apply {
            useController = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return playerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отримуємо дані фільму з DetailsActivity
        val movie = activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as? Movie
        val videoUrl = movie?.videoUrl ?: return
        val title = movie.title ?: "Playback"

        // Створюємо плеєр
        player = SimpleExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            playerView.player = exoPlayer

            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setSubtitleConfigurations(
                    listOf(
                        MediaItem.SubtitleConfiguration.Builder(
                            "https://bitdash-a.akamaihd.net/content/sintel/subtitles/subtitles_en.vtt".toUri()
                        )
                            .setMimeType(MimeTypes.TEXT_VTT)
                            .setLanguage("en")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build()
                    )
                )
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        FirebaseCrashlytics.getInstance().log("Playing video: $title")
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerView.player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
