package com.vs18.tvdemoapp.ui.screens

import android.annotation.*
import android.content.*
import android.net.*
import android.widget.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.player.*

@Composable
fun VideoScreen(movie: Movie?, isOfflineMode: Boolean) {
    val context = LocalContext.current
    val movieState by rememberUpdatedState(movie)
    var errorMessage by remember { mutableStateOf<String>("") }

    VideoPlayer(
        movie = movie,
        onError = { msg -> errorMessage = msg }
    )

    errorMessage?.let {message ->
        LaunchedEffect(message) {
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