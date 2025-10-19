package com.vs18.tvdemoapp

import android.content.*
import android.os.*
import android.view.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vs18.tvdemoapp.core.model.Movie
import com.vs18.tvdemoapp.ui.screens.VideoDetailsScreen

class VideoDetailsFragment : Fragment() {

    private var movie: Movie? = null
    private var isOfflineMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movie = activity?.intent?.getParcelableExtra(DetailsActivity.MOVIE)
        isOfflineMode = activity?.intent?.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false) ?: false

        if (movie == null) {
            FirebaseCrashlytics.getInstance().log("No movie data provided in VideoDetailsScreen")
        } else {
            FirebaseCrashlytics.getInstance().log("Opened details for movie: ${movie!!.title}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FirebaseCrashlytics.getInstance().log("Opened details for movie: ${movie?.title}")
        val app = requireActivity().application as TvDemoApp
        val imageLoader = app.imageLoader

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        VideoDetailsScreen(
                            movie = movie,
                            isOfflineMode = isOfflineMode,
                            imageLoader = imageLoader,
                            onNavigateToPlayer = { selected ->
                                val intent = Intent(requireContext(), PlaybackActivity::class.java)
                                intent.putExtra(DetailsActivity.MOVIE, selected)
                                startActivity(intent)
                            },
                            onNavigateToMain = {
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                        )
                    }
                }
            }
        }
    }
}
