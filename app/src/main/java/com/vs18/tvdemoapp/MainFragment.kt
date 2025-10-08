package com.vs18.tvdemoapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

@Suppress("DEPRECATION")
class MainFragment : BrowseSupportFragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var backgroundManager: BackgroundManager
    private var defaultBackground: Drawable? = null
    private lateinit var metrics: DisplayMetrics
    private var backgroundTimer: Timer? = null
    private var backgroundUri: String? = null
    private val repository by lazy {
        MovieRepository((requireActivity().application as TvDemoApp).database.movieDao())
    }
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var adapter: ArrayObjectAdapter
    private var staticRowsLoaded = false // Флаг для статичних рядків

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)

        adapter = ArrayObjectAdapter(ListRowPresenter())
        setAdapter(adapter) // Встановлюємо адаптер

        scope.launch {
            repository.getMoviesFromNetwork().collect { movies ->
                Log.d(TAG, "Movies from network: $movies")
                updateAdapter(movies)
            }
        }

        scope.launch {
            repository.movieState.collect { movies ->
                Log.d(TAG, "Movies from state: $movies")
                updateAdapter(movies)
            }
        }

        scope.launch {
            repository.movieSelectionEvents.collect { movie ->
                Log.d(TAG, "Movie selected: ${movie.title}")
                val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.MOVIE, movie)
                }
                startActivity(intent)
            }
        }

        title = "Loading..."
        headersState = HEADERS_ENABLED

        prepareBackgroundManager()
        setupUIElements()
        setupEventListeners()
        loadRows() // Завантажуємо статичні рядки одразу
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundTimer?.cancel()
        scope.cancel()
    }

    private fun updateAdapter(movies: List<Movie>) {
        Log.d(TAG, "Updating adapter with movies: $movies")

        // Зберігаємо статичні рядки
        val staticRows = mutableListOf<Row>()
        for (i in 0 until adapter.size()) {
            val row = adapter.get(i) as? Row
            if (row != null && row.headerItem.name != "Movies") {
                staticRows.add(row)
            }
        }

        adapter.clear()
        if (movies.isNotEmpty()) {
            val rowAdapter = ArrayObjectAdapter(CardPresenter()).apply {
                addAll(0, movies)
            }
            adapter.add(ListRow(HeaderItem(0, "Movies"), rowAdapter))
        }

        // Додаємо назад статичні рядки
        staticRows.forEach { adapter.add(it) }

        // Завантажуємо статичні рядки, якщо вони ще не додані
        if (!staticRowsLoaded) {
            loadRows()
        }

        setAdapter(adapter) // Оновлюємо адаптер
        Log.d(TAG, "Adapter updated, size: ${adapter.size()}")
    }

    private fun prepareBackgroundManager() {
        backgroundManager = BackgroundManager.getInstance(requireActivity())
        backgroundManager.attach(requireActivity().window)
        defaultBackground = ContextCompat.getDrawable(requireActivity(), R.drawable.default_background)
        metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
    }

    private fun setupUIElements() {
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_opaque)
    }

    private fun loadRows() {
        if (staticRowsLoaded) return // Запобігаємо повторному додаванню
        staticRowsLoaded = true

        val list = MovieList.list
        Log.d(TAG, "Loading static rows with MovieList: $list")
        val cardPresenter = CardPresenter()

        for (i in 0 until NUM_ROWS) {
            if (i != 0) {
                Collections.shuffle(list)
            }
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until NUM_COLS) {
                listRowAdapter.add(list[j % list.size])
            }
            val header = HeaderItem(i.toLong() + 1, MovieList.MOVIE_CATEGORY[i])
            adapter.add(ListRow(header, listRowAdapter))
        }

        val gridHeader = HeaderItem(NUM_ROWS.toLong() + 1, "PREFERENCES")
        val gridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(gridPresenter)
        gridRowAdapter.add(resources.getString(R.string.grid_view))
        gridRowAdapter.add(resources.getString(R.string.error_fragment))
        gridRowAdapter.add(resources.getString(R.string.personal_settings))
        adapter.add(ListRow(gridHeader, gridRowAdapter))

        setAdapter(adapter) // Оновлюємо адаптер
        Log.d(TAG, "Static rows loaded: ${MovieList.MOVIE_CATEGORY.asList() + "PREFERENCES"}, adapter size: ${adapter.size()}")
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(requireActivity(), "Implement your own in-app search", Toast.LENGTH_LONG).show()
        }
        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is Movie) {
                FirebaseCrashlytics.getInstance().log("Clicked movie: ${item.title}")
                val intent = Intent(activity, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.MOVIE, item)
                }
                val imageView = (itemViewHolder?.view as? ImageCardView)?.mainImageView
                val bundle = imageView?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        it,
                        DetailsActivity.SHARED_ELEMENT_NAME
                    ).toBundle()
                }
                startActivity(intent, bundle)
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(activity, item, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is Movie) {
                backgroundUri = item.backgroundImageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        Glide.with(requireActivity())
            .load(uri)
            .centerCrop()
            .error(defaultBackground)
            .into(object : SimpleTarget<Drawable>(width, height) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    backgroundManager.drawable = resource
                }
            })
        backgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        backgroundTimer?.cancel()
        backgroundTimer = Timer()
        backgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {
        override fun run() {
            handler.post { updateBackground(backgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val view = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
                isFocusable = true
                isFocusableInTouchMode = true
                setBackgroundColor(ContextCompat.getColor(context, R.color.default_background))
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }
            return ViewHolder(view)
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