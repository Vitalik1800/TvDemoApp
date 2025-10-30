package com.vs18.tvdemoapp.security

import android.content.*
import android.util.Log
import androidx.security.crypto.*

object SecurePrefs {
    private const val FILE_NAME = "secure_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        } catch (e: Exception) {
            Log.w("SecurePrefs", "Using regular SharedPreferences (test mode)")
            prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    var userToken: String?
        get() = prefs.getString("user_token", null)
        set(value) = prefs.edit().putString("user_token", value).apply()

    var lastPlayedMovieId: Long
        get() = prefs.getLong("last_played_movie_id", -1)
        set(value) = prefs.edit().putLong("last_played_movie_id", value).apply()
}
