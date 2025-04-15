package com.omdbmovies.movieverse.data.Repository


import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.omdbmovies.movieverse.data.Local.MovieDao
import com.omdbmovies.movieverse.data.ApiService.MovieApiService
import com.omdbmovies.movieverse.data.Local.model.OfflineMovieResponse
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val api: MovieApiService,
    private val movieDao: MovieDao
) {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun getMovieByTitle(title: String) = api.searchMovies(title)

     suspend fun saveMoviesToDb(movies: List<OfflineMovieResponse>) {
        movieDao.insertMovies(movies)
    }

    suspend fun getMoviesFromDb(): List<OfflineMovieResponse> {
        return movieDao.getAllMovies()
    }
}
