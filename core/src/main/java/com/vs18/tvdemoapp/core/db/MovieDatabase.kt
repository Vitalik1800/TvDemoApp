package com.vs18.tvdemoapp.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vs18.tvdemoapp.core.model.*

@Database(entities = [Movie::class], version = 1)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}