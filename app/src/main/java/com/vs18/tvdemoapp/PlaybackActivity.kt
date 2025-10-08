package com.vs18.tvdemoapp

import android.os.*
import androidx.fragment.app.*

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)
        if (savedInstanceState == null) {
            val fragment = PlaybackVideoFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.playback_fragment, fragment)
                .commitNow()
        }
    }
}