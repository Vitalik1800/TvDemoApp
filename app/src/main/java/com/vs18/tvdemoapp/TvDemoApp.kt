package com.vs18.tvdemoapp

import android.annotation.SuppressLint
import android.app.*
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.vs18.tvdemoapp.core.di.*
import org.koin.core.context.*
import org.koin.android.ext.koin.*

class TvDemoApp : Application(){

    lateinit var imageLoader: ImageLoader
        private set

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TvDemoApp)
            modules(coreModule)
        }

        imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(ImageDecoderDecoder.Factory())
                add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .logger(DebugLogger())
            .build()
    }

}