package com.vs18.ml

import android.content.*
import android.util.*
import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

object MLKitTranslationManager {
    private var translator: Translator? = null
    private var isModelReady = false

    fun init(context: Context) {
        if (translator != null) return

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.UKRAINIAN)
            .build()

        val client = Translation.getClient(options)
        client.downloadModelIfNeeded()
            .addOnSuccessListener {
                Log.d("MLKit", "Model downloaded (EN to UK)")
                translator = client
                isModelReady = true
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Model downloaded failed", e)
            }
    }

    suspend fun translate(text: String): String = suspendCancellableCoroutine { cont ->
        if (text.isBlank()) {
            cont.resume(text)
            return@suspendCancellableCoroutine
        }

        val t = translator ?: run {
            cont.resume("Model not ready")
            return@suspendCancellableCoroutine
        }

        t.translate(text)
            .addOnSuccessListener { translated ->
                cont.resume(translated)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    fun isReady(): Boolean = isModelReady
}