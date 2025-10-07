package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.*
import com.google.firebase.remoteconfig.*

class MainActivity : FragmentActivity() {

    private lateinit var errorFragment: ErrorFragment
    private lateinit var spinnerFragment: SpinnerFragment
    private lateinit var remoteConfig: FirebaseRemoteConfig

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val crashButton = Button(this).apply {
            text = "Crash Test"
            setOnClickListener {
                FirebaseCrashlytics.getInstance().log("Crash button pressed in MainActivity")
                throw RuntimeException("Test Crash: triggered by Crash Button")
            }
        }

        addContentView(
            crashButton,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

        FirebaseCrashlytics.getInstance().log("Testing Crashlytics crash")

        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf("catalog_title" to "Default Catalog Title")
        )

        fetchRemoteConfig()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
       // testError()
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("RemoteConfig", "Config params updated: $updated")
                    val catalogTitle = remoteConfig.getString("catalog_title")
                    Log.d("RemoteConfig", "catalog_title = $catalogTitle")

                    supportFragmentManager.setFragmentResult(
                        "remoteConfig",
                        Bundle().apply { putString("catalog_title", catalogTitle) }
                    )
                } else {
                    Log.w("RemoteConfig", "Fetch Failed")
                }
            }
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