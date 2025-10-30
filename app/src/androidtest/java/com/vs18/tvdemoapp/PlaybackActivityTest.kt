package com.vs18.tvdemoapp

import android.content.*
import androidx.test.core.app.*
import androidx.test.ext.junit.rules.*
import androidx.test.ext.junit.runners.*
import com.vs18.tvdemoapp.core.model.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackActivityTest {

    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val movie = Movie(
        id = 1,
        title = "Big Buck Bunny",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/main/bigbuckbunny_en.vtt"
    )

    private val intent = Intent(appContext, PlaybackActivity::class.java).apply {
        putExtra(DetailsActivity.MOVIE, movie)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @get:Rule
    val activityRule = ActivityScenarioRule<PlaybackActivity>(intent)

    @Test
    fun test_PlaybackVideoFragment_is_displayed() {
        activityRule.scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.playback_fragment)
            check(fragment is PlaybackVideoFragment) {
                "Excepted PlaybackVideoFragment, but got ${fragment?.javaClass?.simpleName}"
            }
        }
    }

}