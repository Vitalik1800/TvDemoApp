package com.vs18.tvdemoapp.core.di

import android.content.*
import androidx.room.*
import com.vs18.tvdemoapp.core.db.*
import com.vs18.tvdemoapp.core.repository.*
import org.koin.dsl.*

val coreModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            MovieDatabase::class.java,
            "tvdemoapp-db"
        ).build()
    }
    single { get<MovieDatabase>().movieDao() }
    single { MovieRepository(get(), get()) }
}