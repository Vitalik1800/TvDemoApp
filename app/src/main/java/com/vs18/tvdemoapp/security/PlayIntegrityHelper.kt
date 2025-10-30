package com.vs18.tvdemoapp.security

import android.content.*
import com.google.android.play.core.integrity.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import com.vs18.tvdemoapp.BuildConfig

class PlayIntegrityHelper(private val context: Context) {

    suspend fun verifyAppIntegrity(): Boolean = suspendCancellableCoroutine { cont ->

        if (BuildConfig.DEBUG) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }

        val integrityHelper = IntegrityManagerFactory.create(context)
        val nonce: String? = "nonce-${System.currentTimeMillis()}".toByteArray().toString()

        integrityHelper.requestIntegrityToken(
            IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .setCloudProjectNumber(123456789012L)
                .build()
        ).addOnSuccessListener { response ->
            cont.resume(parseJwt(response.token()))
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
    }

    private fun parseJwt(jwt: String): Boolean {
        return try {
            val payload = jwt.split(".")[1]
            val json = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            json.contains("\"appIntegrity\":\"INTEGRITY_PASS\"")
        } catch (e: Exception) {
            false
        }
    }
}