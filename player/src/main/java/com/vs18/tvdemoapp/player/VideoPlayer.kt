package com.vs18.tvdemoapp.player

import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.viewinterop.*
import androidx.core.net.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.*
import com.google.android.exoplayer2.util.*
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*

@Composable
fun VideoPlayer(
    movie: Movie?,
    isOfflineMode: Boolean,
    isNetworkAvailable: () -> Boolean,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val playerState by rememberUpdatedState(movie)

    if (movie == null || movie.videoUrl.isNullOrEmpty()) {
        LaunchedEffect(Unit) {
            FirebaseCrashlytics.getInstance().log("No movie data or video URL")
            onError("Error: No movie data")
        }
        return
    }

    if (isOfflineMode || !isNetworkAvailable()) {
        LaunchedEffect(Unit) {
            FirebaseCrashlytics.getInstance().log("Attempted to play video offline: ${movie.title}")
            onError("Video playback is unavailable in offline mode.")
        }
        return
    }

    val player by remember { mutableStateOf(SimpleExoPlayer.Builder(context).build()) }
    val playerView = remember {
        PlayerView(context).apply {
            useController = true
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            player.playWhenReady = !player.playWhenReady
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            player.seekTo(player.currentPosition - 10000)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            player.seekTo(player.currentPosition + 10000)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
        }
    }

    LaunchedEffect(playerState) {
        try {
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
                    onError("Failed to load subtitles for ${movie.title ?: "unknown"}")
                }
            }
            player.setMediaItem(builder.build())
            player.prepare()
            player.playWhenReady = true
            FirebaseCrashlytics.getInstance().log("Playing video: ${movie.title ?: "unknown"}")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            onError("Failed to play video: ${e.message ?: "Unknown error"}")
        }
    }

    AndroidView(factory = { playerView }) { view ->
        view.player = player
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            playerView.player = null
        }
    }
}