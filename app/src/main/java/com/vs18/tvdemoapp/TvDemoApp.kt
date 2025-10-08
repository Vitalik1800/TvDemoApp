package com.vs18.tvdemoapp

import android.app.*
import androidx.room.*

class TvDemoApp : Application() {

    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tvdemoapp-db"
        ).build()
    }

}