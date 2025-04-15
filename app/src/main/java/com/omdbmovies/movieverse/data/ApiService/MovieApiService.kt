package com.omdbmovies.movieverse.data.ApiService

import com.omdbmovies.movieverse.BuildConfig
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("/")
    suspend fun searchMovies(
        @Query("t") title: String,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): Response<MoviesResponse>

}