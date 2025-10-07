package com.vs18.tvdemoapp

import android.content.*
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.*
import org.junit.*
import org.junit.runner.*

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

    @Test
    fun test_PlaybackVideoFragment_is_displayed() {
        onView(withId(R.id.exo_player_view)).check(matches(isDisplayed()))
    }
}