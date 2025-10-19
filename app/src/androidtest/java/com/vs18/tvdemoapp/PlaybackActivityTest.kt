package com.vs18.tvdemoapp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vs18.tvdemoapp.core.model.Movie
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PlaybackActivityTest {

    private val movie = Movie(
        id = 1,
        title = "Big Buck Bunny",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        subtitleUrl = "https://raw.githubusercontent.com/Vitalik1800/SubtitlesVS18/main/bigbuckbunny_en.vtt"
    )

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val intent = Intent(context, PlaybackActivity::class.java).apply {
        putExtra(DetailsActivity.MOVIE, movie)
    }

    @get:Rule
    val activityRule = ActivityScenarioRule<PlaybackActivity>(intent)

    private val idlingResource = CountingIdlingResource("FragmentTransaction")

    @Test
    fun test_PlaybackVideoFragment_is_displayed() {
        IdlingPolicies.setMasterPolicyTimeout(30, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(30, TimeUnit.SECONDS)

        IdlingRegistry.getInstance().register(idlingResource)

        activityRule.scenario.onActivity { activity ->
            idlingResource.increment()
            activity.supportFragmentManager.executePendingTransactions()
            if (activity.supportFragmentManager.findFragmentById(R.id.playback_fragment) is PlaybackVideoFragment) {
                idlingResource.decrement()
            } else {
                activity.supportFragmentManager.addOnBackStackChangedListener {
                    if (activity.supportFragmentManager.findFragmentById(R.id.playback_fragment) is PlaybackVideoFragment) {
                        idlingResource.decrement()
                    }
                }
            }
        }
    }
}