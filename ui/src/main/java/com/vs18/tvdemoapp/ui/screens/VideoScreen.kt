package com.vs18.tvdemoapp.ui.screens

import android.annotation.*
import android.content.*
import android.net.*
import android.widget.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.player.*

@Composable
fun VideoScreen(
    movie: Movie?,
    isOfflineMode: Boolean
) {
    val context = LocalContext.current
    val crashlytics = remember { FirebaseCrashlytics.getInstance() }
    val networkAvailable by remember { derivedStateOf { context.isNetworkAvailable() } }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movie, isOfflineMode, networkAvailable) {
        when {
            movie == null || movie.videoUrl.isNullOrEmpty() -> {
                errorMessage = "Error: No movie data"
            }
            isOfflineMode || !networkAvailable -> {
                errorMessage = "Video playback unavailable in offline mode."
            }
        }
    }

    if (errorMessage == null && movie != null) {
        ImaAdsVideoPlayer(
            movie = movie,
            isOfflineMode = isOfflineMode,
            isNetworkAvailable = { networkAvailable },
            onError = { msg -> errorMessage = msg }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            crashlytics.log("VideoScreen error: $msg")
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }
}

@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}