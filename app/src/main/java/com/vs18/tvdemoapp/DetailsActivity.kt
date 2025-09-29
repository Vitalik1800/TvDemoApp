package com.vs18.tvdemoapp

import android.os.*
import android.widget.*
import androidx.fragment.app.*

class DetailsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        if (savedInstanceState == null) {
            val fragment = VideoDetailsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.details_fragment, fragment)
                .commit()
        }
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "hero"
        const val MOVIE = "Movie"
    }
}