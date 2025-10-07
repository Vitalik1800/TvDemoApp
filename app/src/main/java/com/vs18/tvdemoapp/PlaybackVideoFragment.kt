package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.os.*
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.*
import com.google.android.exoplayer2.util.*
import com.google.firebase.crashlytics.*

class PlaybackVideoFragment : Fragment() {

    private var player: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playerView = inflater.inflate(R.layout.exo_simple_player_view, container, false) as PlayerView
        playerView.useController = true
        return playerView
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener{ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        player?.let { it.playWhenReady = !it.playWhenReady}
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }

        val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE, Movie::class.java)
        } else {
            @Suppress("DEPRECATION")
            activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as? Movie
        }
        val videoUrl = movie?.videoUrl ?: return
        val title = movie.title ?: "Playback"

        player = SimpleExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            playerView.player = exoPlayer

            val builder = MediaItem.Builder().setUri(videoUrl)

            movie.subtitleUrl?.let { subs ->
                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subs.toUri())
                    .setMimeType(MimeTypes.TEXT_VTT)
                    .setLanguage("en")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()

                builder.setSubtitleConfigurations(listOf(subtitleConfig))
            }

            val mediaItem = builder.build()

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