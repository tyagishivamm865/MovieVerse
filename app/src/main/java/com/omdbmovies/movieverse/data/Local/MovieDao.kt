package com.omdbmovies.movieverse.data.Local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omdbmovies.movieverse.data.Local.model.OfflineMovieResponse

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertMovies(movies: List<OfflineMovieResponse>)

    @Query("SELECT * FROM movies_table")
     suspend fun getAllMovies(): List<OfflineMovieResponse>

    @Query("SELECT * FROM movies_table WHERE title = :title")
     suspend fun getMovieByTitle(title: String): OfflineMovieResponse?
}