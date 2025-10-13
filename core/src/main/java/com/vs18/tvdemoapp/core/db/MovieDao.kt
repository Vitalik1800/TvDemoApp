package com.vs18.tvdemoapp.core.db

import androidx.room.*
import com.vs18.tvdemoapp.core.model.*
import kotlinx.coroutines.flow.*

@Dao
interface MovieDao {

    @Query("SELECT * FROM Movie")
    fun getAll(): Flow<List<Movie>>

    @Query("SELECT * FROM Movie WHERE videoUrl IS NULL")
    suspend fun getOfflineMovies(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(movies: List<Movie>)
}