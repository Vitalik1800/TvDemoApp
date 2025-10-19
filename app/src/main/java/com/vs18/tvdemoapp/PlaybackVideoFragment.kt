package com.vs18.tvdemoapp

import android.os.*
import android.view.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.ui.screens.VideoScreen

@Suppress("DEPRECATION")
class PlaybackVideoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val movie = arguments?.getParcelable<Movie>(DetailsActivity.MOVIE)
        if (movie == null) {
            FirebaseCrashlytics.getInstance().log("No movie data in PlaybackVideoFragment")
        } else {
            FirebaseCrashlytics.getInstance().log("Playing video: ${movie.title}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val movie = arguments?.getParcelable<Movie>(DetailsActivity.MOVIE)
        val isOfflineMode = arguments?.getBoolean(DetailsActivity.IS_OFFLINE_MODE, false) ?: false

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

    companion object {
        fun newInstance(movie: Movie?, isOfflineMode: Boolean): PlaybackVideoFragment {
            return PlaybackVideoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DetailsActivity.MOVIE, movie)
                    putBoolean(DetailsActivity.IS_OFFLINE_MODE, isOfflineMode)
                }
            }
        }
    }

}