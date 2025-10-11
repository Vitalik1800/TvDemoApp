package com.vs18.tvdemoapp

import android.content.*
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.util.*
import android.widget.*
import androidx.core.app.*
import androidx.core.content.*
import androidx.leanback.app.*
import androidx.leanback.widget.*
import com.bumptech.glide.*
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.transition.*
import com.google.firebase.crashlytics.*
import java.util.*
import kotlin.math.roundToInt

class VideoDetailsFragment : DetailsSupportFragment() {

    private var selectedMovie: Movie? = null
    private lateinit var presenterSelector: ClassPresenterSelector
    private lateinit var adapter: ArrayObjectAdapter
    private var isOfflineMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate DetailsFragment")
        super.onCreate(savedInstanceState)

        selectedMovie = activity?.intent?.getParcelableExtra(DetailsActivity.MOVIE)
        isOfflineMode = activity?.intent?.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false) ?: false

        if (selectedMovie != null) {
            presenterSelector = ClassPresenterSelector()
            adapter = ArrayObjectAdapter(presenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupRelatedMovieListRow()
            onItemViewClickedListener = ItemViewClickedListener()
            setAdapter(adapter)
            FirebaseCrashlytics.getInstance().log("Opened details for movie: ${selectedMovie?.title}")
        } else {
            FirebaseCrashlytics.getInstance().log("No movie data provided in VideoDetailsFragment")
            Toast.makeText(requireContext(), "Error: No movie data", Toast.LENGTH_LONG).show()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDetailsOverviewRow() {
        val row = DetailsOverviewRow(selectedMovie!!)
        row.imageDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.default_background)
        val width = convertDpToPixel(requireActivity(), DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(requireActivity(), DETAIL_THUMB_HEIGHT)
        Glide.with(requireActivity())
            .load(selectedMovie?.cardImageUrl)
            .centerCrop()
            .error(R.drawable.default_background)
            .into(object : SimpleTarget<Drawable>(width, height) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    row.imageDrawable = resource
                    adapter.notifyArrayItemRangeChanged(0, adapter.size())
                }
            })

        val actionAdapter = ArrayObjectAdapter()
        if (!isOfflineMode && isNetworkAvailable() && !selectedMovie!!.videoUrl!!.isEmpty()) {
            actionAdapter.add(
                Action(
                    ACTION_WATCH,
                    resources.getString(R.string.watch),
                    resources.getString(R.string.watch_description)
                )
            )
        } else {
            FirebaseCrashlytics.getInstance().log("Video playback unavailable for: ${selectedMovie?.title}")
            Toast.makeText(requireContext(), "Video playback is unavailable in offline mode.", Toast.LENGTH_LONG).show()
        }
        row.actionsAdapter = actionAdapter
        adapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor = ContextCompat.getColor(requireActivity(), R.color.selected_background)
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(activity, DetailsActivity.SHARED_ELEMENT_NAME)
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true
        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == ACTION_WATCH) {
                if (!isNetworkAvailable() || selectedMovie!!.videoUrl!!.isEmpty()) {
                    Toast.makeText(requireContext(), "Video playback is unavailable in offline mode.", Toast.LENGTH_LONG).show()
                    FirebaseCrashlytics.getInstance().log("Attempted to play video offline: ${selectedMovie?.title}")
                    return@OnActionClickedListener
                }
                val intent = Intent(activity, PlaybackActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, selectedMovie)
                startActivity(intent)
                FirebaseCrashlytics.getInstance().log("Watch action for movie: ${selectedMovie?.title}")
            } else {
                Toast.makeText(activity, action.toString(), Toast.LENGTH_LONG).show()
            }
        }
        presenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupRelatedMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list
        Collections.shuffle(list)
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0 until NUM_COLS) {
            listRowAdapter.add(list[j % list.size])
        }
        val header = HeaderItem(0, subcategories[0])
        adapter.add(ListRow(header, listRowAdapter))
        presenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun convertDpToPixel(context: Context, dp: Int) : Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is Movie) {
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)
                intent.putExtra(DetailsActivity.IS_OFFLINE_MODE, isOfflineMode)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    (itemViewHolder?.view as ImageCardView).mainImageView!!,
                    DetailsActivity.SHARED_ELEMENT_NAME
                ).toBundle()
                startActivity(intent, bundle)
                FirebaseCrashlytics.getInstance().log("Clicked related movie: ${item.title}")
            }
        }
    }

    companion object {
        private const val TAG = "VideoDetailsFragment"
        private const val ACTION_WATCH = 1L
        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274
        private const val NUM_COLS = 10
    }
}