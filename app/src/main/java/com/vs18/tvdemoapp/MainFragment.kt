package com.vs18.tvdemoapp

import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.*
import android.util.*
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.core.app.*
import androidx.core.content.*
import androidx.leanback.app.*
import androidx.leanback.widget.*
import com.bumptech.glide.*
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.transition.*
import com.google.firebase.crashlytics.*
import com.google.firebase.remoteconfig.*
import com.vs18.tvdemoapp.core.model.*
import com.vs18.tvdemoapp.core.model.Movie
import com.vs18.tvdemoapp.core.repository.*
import com.vs18.tvdemoapp.security.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import org.koin.android.ext.android.*
import java.util.*

@Suppress("DEPRECATION")
class MainFragment : BrowseSupportFragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var backgroundManager: BackgroundManager
    private var defaultBackground: Drawable? = null
    private lateinit var metrics: DisplayMetrics
    private var backgroundJob: Job? = null
    private var backgroundUri: String? = null

    private val repository: MovieRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var adapter: ArrayObjectAdapter
    private var staticRowsLoaded = false
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var isOfflineMode = false
    private var isAppVerified: Boolean? = null

    private val playIntegrityHelper by lazy { PlayIntegrityHelper(requireContext()) }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = ArrayObjectAdapter(ListRowPresenter())
        setAdapter(adapter)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 })
        remoteConfig.setDefaultsAsync(mapOf("catalog_title" to "TvDemoApp"))

        title = "Loading..."
        fetchRemoteConfig()

        scope.launch {
            isAppVerified = playIntegrityHelper.verifyAppIntegrity()
            if (isAppVerified == false) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Security check failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        scope.launch {
            repository.getMoviesFromNetwork()
                .catch { e ->
                    Log.e(TAG, "Network error: ${e.message}", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    isOfflineMode = true
                    updateAdapter(emptyList())
                    withContext(Dispatchers.Main) {
                       Toast.makeText(requireContext(), "Offline mode", Toast.LENGTH_LONG).show()
                    }
                }
                .collect { movies ->
                    isOfflineMode = movies.all { it.videoUrl.isNullOrEmpty() }
                    updateAdapter(movies)
                }
        }

        headersState = HEADERS_ENABLED
        prepareBackgroundManager()
        setupUIElements()
        setupEventListeners()
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val title = if (task.isSuccessful) {
                    remoteConfig.getString("catalog_title").ifEmpty { "TvDemoApp" }
                } else {
                    "TvDemoApp"
                }
                animateTitleChange(title)
            }
    }

    private fun animateTitleChange(newTitle: String) {
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply { duration = 300; fillAfter = true }
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply { duration = 300; fillAfter = true }
        titleView?.startAnimation(fadeOut)
        handler.postDelayed({
            title = newTitle
            titleView?.startAnimation(fadeIn)
        }, 300)
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundJob?.cancel()
        scope.cancel()
    }

    private fun updateAdapter(movies: List<Movie>) {
        if (isAppVerified == false && BuildConfig.FLAVOR == "pro") {
            adapter.clear()
            adapter.add(ListRow(HeaderItem(0, "Security Error"), ArrayObjectAdapter(CardPresenter())))
            return
        }

        val staticRows = mutableListOf<Row>()
        for (i in 0 until adapter.size()) {
            val row = adapter.get(i) as? Row
            if (row?.headerItem?.name != "Movies" && row?.headerItem?.name != "Offline Movies") {
                staticRows.add(row!!)
            }
        }

        adapter.clear()
        if (movies.isNotEmpty()) {
            val rowAdapter = ArrayObjectAdapter(CardPresenter()).apply { addAll(0, movies) }
            val header = if (isOfflineMode) "Offline Movies" else "Movies"
            adapter.add(ListRow(HeaderItem(0, header), rowAdapter))
        }

        if (!staticRowsLoaded) loadRows()
        staticRows.forEach { adapter.add(it) }
        setAdapter(adapter)
    }

    private fun prepareBackgroundManager() {
        backgroundManager = BackgroundManager.getInstance(requireActivity()).apply {
            attach(requireActivity().window)
        }
        defaultBackground = ContextCompat.getDrawable(requireActivity(), R.drawable.default_background)
        metrics = DisplayMetrics().also {
            requireActivity().windowManager.defaultDisplay.getMetrics(it)
        }
    }

    private fun setupUIElements() {
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_opaque)
    }

    private fun loadRows() {
        if (staticRowsLoaded) return
        staticRowsLoaded = true

        val list = MovieList.list
        val cardPresenter = CardPresenter()

        for (i in 0 until NUM_ROWS) {
            if (i != 0) Collections.shuffle(list)
            val rowAdapter = ArrayObjectAdapter(cardPresenter)
            repeat(NUM_COLS) { rowAdapter.add(list[it % list.size]) }
            adapter.add(ListRow(HeaderItem(i.toLong() + 1, MovieList.MOVIE_CATEGORY[i]), rowAdapter))
        }

        val gridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(gridPresenter).apply {
            add(resources.getString(R.string.grid_view))
            add(resources.getString(R.string.error_fragment))
            add(resources.getString(R.string.personal_settings))
        }
        adapter.add(ListRow(HeaderItem(NUM_ROWS.toLong() + 1, "PREFERENCES"), gridRowAdapter))
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(requireActivity(), "Search not implemented", Toast.LENGTH_LONG).show()
        }
        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            if (item is Movie) {
                val intent = Intent(activity, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.MOVIE, item)
                    putExtra(DetailsActivity.IS_OFFLINE_MODE, isOfflineMode)
                }
                val imageView = (itemViewHolder?.view as? ImageCardView)?.mainImageView
                val bundle = imageView?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(), it, DetailsActivity.SHARED_ELEMENT_NAME
                    ).toBundle()
                }
                startActivity(intent, bundle)
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            if (item is Movie) {
                backgroundUri = item.backgroundImageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun startBackgroundTimer() {
        backgroundJob?.cancel()
        backgroundJob = scope.launch {
            delay(BACKGROUND_UPDATE_DELAY.toLong())
            if (isAdded) updateBackground(backgroundUri)
        }
    }

    private fun updateBackground(uri: String?) {
        Glide.with(requireActivity())
            .load(uri)
            .centerCrop()
            .error(defaultBackground)
            .into(object : SimpleTarget<Drawable>(metrics.widthPixels, metrics.heightPixels) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    backgroundManager.drawable = resource
                }
            })
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            return ViewHolder(TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
                isFocusable = true
                setBackgroundColor(ContextCompat.getColor(context, R.color.default_background))
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }

    companion object {
        private const val TAG = "MainFragment"
        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
        private const val NUM_ROWS = 3
        private const val NUM_COLS = 5
    }
}