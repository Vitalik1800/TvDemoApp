package com.vs18.tvdemoapp.player

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.viewinterop.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.ima.*
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.ui.*
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*
import androidx.core.net.toUri
import kotlin.random.Random

@Composable
fun ImaAdsVideoPlayer(
    movie: Movie?,
    isOfflineMode: Boolean,
    isNetworkAvailable: () -> Boolean,
    onError: (String) -> Unit
) {
    val context = LocalContext.current

    if (movie == null || movie.videoUrl.isNullOrEmpty() || isOfflineMode || !isNetworkAvailable()) {
        LaunchedEffect(Unit) {
            onError("Cannot play offline movies")
        }
        return
    }

    val adsLoader = remember {
        ImaAdsLoader.Builder(context)
            .setAdErrorListener { error ->
                FirebaseCrashlytics.getInstance().recordException(error as Throwable)
            }
            .build()
    }

    val mediaSourceFactory = remember {
        DefaultMediaSourceFactory(context)
            .setAdsLoaderProvider { adsLoader }
    }

    val player = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                adsLoader.setPlayer(this)
            }
    }

    val playerView = remember {
        PlayerView(context).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
    }

    val adTagUri = remember {
        val correlator = Random.nextLong()
        ("https://pubads.g.doubleclick.net/gampad/ads?" +
                "iu=/21775744923/exoplayer_preroll&" +
                "description_url=https%3A%2F%2Fdevelopers.google.com%2Finteractive-media-ads&" + // РЕАЛЬНИЙ URL
                "tfcd=0&npa=0&sz=640x480&gdfp_req=1&output=vast&" +
                "unviewed_position_start=1&env=vp&impl=s&" +
                "correlator=$correlator").toUri()
    }

    LaunchedEffect(movie) {
        try {

            val mediaItem = MediaItem.Builder()
                .setUri(movie.videoUrl!!.toUri())
                .setAdTagUri(adTagUri)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true

            FirebaseCrashlytics.getInstance().log("IMA Ads: Video + ads loaded")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            onError("Ads integration failed: ${e.message}")
        }
    }

    AndroidView(
        factory = { playerView }
    ) { view ->
        view.player = player
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            adsLoader.release()
        }
    }
}