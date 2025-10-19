package com.vs18.tvdemoapp.ui.screens

import android.annotation.*
import android.content.*
import android.net.*
import android.widget.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.player.*

@Composable
fun VideoScreen(movie: Movie?, isOfflineMode: Boolean) {
    val context = LocalContext.current
    val movieState by rememberUpdatedState(movie)
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(movieState, isOfflineMode) {
        if (movieState == null || movieState!!.videoUrl.isNullOrEmpty()) {
            FirebaseCrashlytics.getInstance().log("No movie data or video URL in VideoScreen")
            errorMessage = "Error: No movie data"
        } else if (isOfflineMode || !context.isNetworkAvailable()) {
            FirebaseCrashlytics.getInstance().log("Attempted to play video offline: ${movieState!!.title}")
            errorMessage = "Video playback is unavailable in offline mode."
        }
    }

    VideoPlayer(
        movie = movieState,
        isOfflineMode = isOfflineMode,
        isNetworkAvailable = { context.isNetworkAvailable()},
        onError = { msg -> errorMessage = msg }
    )

    errorMessage?.let {message ->
        LaunchedEffect(message) {
            FirebaseCrashlytics.getInstance().log("VideoScreen error: $message")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            errorMessage = ""
        }
    }
}

@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}