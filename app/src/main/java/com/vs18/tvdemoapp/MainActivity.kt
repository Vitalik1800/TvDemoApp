package com.vs18.tvdemoapp

import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import com.google.firebase.crashlytics.*
import com.google.firebase.remoteconfig.*

class MainActivity : FragmentActivity() {

    private lateinit var errorFragment: ErrorFragment
    private lateinit var spinnerFragment: SpinnerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

        FirebaseCrashlytics.getInstance().log("Testing Crashlytics crash")

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(90)
                .build()

        )

        remoteConfig.setDefaultsAsync(mapOf("catalog_title" to "TV Catalog Demo"))
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val title = remoteConfig.getString("catalog_title")
                Toast.makeText(this, "Remove Config title: $title", Toast.LENGTH_LONG).show()
            } else {
                FirebaseCrashlytics.getInstance().recordException(Exception("Remote Config fetch failed"))
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
            // testError()
    }

    private fun testError() {
        errorFragment = ErrorFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_browse_fragment, errorFragment)
            .commit()

        spinnerFragment = SpinnerFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_browse_fragment, spinnerFragment)
            .commit()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            supportFragmentManager
                .beginTransaction()
                .remove(spinnerFragment)
                .commit()
            errorFragment.setErrorContent()
        }, TIMER_DELAY)
    }

    class SpinnerFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val progressBar = ProgressBar(container?.context)
            if (container is FrameLayout) {
                val layoutParams = FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, Gravity.CENTER)
                progressBar.layoutParams = layoutParams
            }
            return progressBar
        }
    }

    companion object {
        private const val TIMER_DELAY = 3000L
        private const val SPINNER_WIDTH = 100
        private const val SPINNER_HEIGHT = 100
    }

}