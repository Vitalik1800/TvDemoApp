package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.view.*
import android.widget.Toast
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

    var player: SimpleExoPlayer? = null
    lateinit var playerView: PlayerView

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
        val isOfflineMode = activity?.intent?.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false) ?: false

        if (movie == null || movie.videoUrl.isNullOrEmpty()) {
            FirebaseCrashlytics.getInstance().log("No movie data or video URL in PlaybackVideoFragment")
            Toast.makeText(requireContext(), "Error: No movie data", Toast.LENGTH_LONG).show()
            requireActivity().finish()
            return
        }

        if (isOfflineMode || !isNetworkAvailable()) {
            FirebaseCrashlytics.getInstance().log("Attempted to play video offline: ${movie.title}")
            Toast.makeText(requireContext(), "Video playback is unavailable in offline mode.", Toast.LENGTH_LONG).show()
            requireActivity().finish()
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

                movie.subtitleUrl?.takeIf { it.isNotEmpty() }?.let { subs ->
                    try {
                        val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subs.toUri())
                            .setMimeType(MimeTypes.TEXT_VTT)
                            .setLanguage("en")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build()
                        builder.setSubtitleConfigurations(listOf(subtitleConfig))
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Toast.makeText(requireContext(), "Failed to load subtitles for $title", Toast.LENGTH_LONG).show()
                    }
                }

                val mediaItem = builder.build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }

        FirebaseCrashlytics.getInstance().log("Playing video: $title")
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        playerView.player = null
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}