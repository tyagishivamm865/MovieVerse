package com.omdbmovies.movieverse.di

import android.app.Application
import androidx.room.Room
import com.omdbmovies.movieverse.data.Local.FavoriteMovieDao
import com.omdbmovies.movieverse.data.Local.MovieDao
import com.omdbmovies.movieverse.data.Local.MovieDatabase
import com.omdbmovies.movieverse.data.ApiService.MovieApiService
import com.omdbmovies.movieverse.data.Repository.FavoriteMovieRepository
import com.omdbmovies.movieverse.data.Repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://www.omdbapi.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApi(retrofit: Retrofit): MovieApiService {
        return retrofit.create(MovieApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMovieDatabase(application: Application): MovieDatabase {
        return Room.databaseBuilder(
            application,
            MovieDatabase::class.java,
            "movie_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(movieDatabase: MovieDatabase): MovieDao {
        return movieDatabase.movieDao()
    }

    @Provides
    @Singleton
    fun provideMovieRepository(
        apiService: MovieApiService,
        movieDao: MovieDao
    ): MovieRepository {
        return MovieRepository(apiService, movieDao)
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(db: MovieDatabase): FavoriteMovieDao {
        return db.favoriteMovieDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(dao: FavoriteMovieDao): FavoriteMovieRepository{
        return FavoriteMovieRepository(dao)
    }

}
