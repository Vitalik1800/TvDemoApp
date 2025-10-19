package com.vs18.tvdemoapp

import android.os.*
import androidx.fragment.app.*
import com.vs18.tvdemoapp.core.model.Movie

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)
        if (savedInstanceState == null) {
            val movie = intent.getParcelableExtra<Movie>(DetailsActivity.MOVIE)
            val isOfflineMode = intent.getBooleanExtra(DetailsActivity.IS_OFFLINE_MODE, false)
            val fragment = PlaybackVideoFragment.newInstance(movie, isOfflineMode)
            supportFragmentManager.beginTransaction()
                .replace(R.id.playback_fragment, fragment)
                .commit()
        }
    }
}