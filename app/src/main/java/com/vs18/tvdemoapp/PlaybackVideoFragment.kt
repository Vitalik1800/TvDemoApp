package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.*
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.*
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.ui.screens.VideoScreen

@Suppress("DEPRECATION")
class PlaybackVideoFragment : Fragment() {

    var movie: Movie? = null
    var isOfflineMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        movie = activity?.intent?.getParcelableExtra(DetailsActivity.MOVIE)
        isOfflineMode = activity?.intent?.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false) ?: false

        if (movie == null) {
            FirebaseCrashlytics.getInstance().log("No movie data in PlaybackVideoFragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        VideoScreen(
                            movie = movie,
                            isOfflineMode = isOfflineMode
                        )
                    }
                }
            }
        }
    }

}