package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.os.*
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.*
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.*
import com.google.firebase.crashlytics.*

@Suppress("DEPRECATION")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        player?.let { it.playWhenReady = !it.playWhenReady }
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }

        val movie: Movie? = activity?.intent?.getParcelableExtra<Movie>(DetailsActivity.MOVIE)
        if (movie == null || movie.videoUrl == null) {
            return
        }
        val title = movie.title ?: "Playback"

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val unstableDataSourceFactory = UnstableDataSourceFactory(httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(unstableDataSourceFactory)

        player = SimpleExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { exoPlayer ->
                playerView.player = exoPlayer

                val builder = MediaItem.Builder().setUri(movie.videoUrl)

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