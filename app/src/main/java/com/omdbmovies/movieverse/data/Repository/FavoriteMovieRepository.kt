package com.omdbmovies.movieverse.data.Repository

import androidx.lifecycle.LiveData
import com.omdbmovies.movieverse.data.Local.FavoriteMovieDao
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import javax.inject.Inject

class FavoriteMovieRepository @Inject constructor(
    private val dao: FavoriteMovieDao
) {
    suspend fun addToFavorites(movie: FavoriteMovieEntity) = dao.insert(movie)
    suspend fun removeFromFavorites(movie: FavoriteMovieEntity) = dao.delete(movie)
    fun isFavoriteLive(imdbId: String): LiveData<Boolean> {
        return dao.isFavorite(imdbId)
    }
    suspend fun getAllFavorites(): List<FavoriteMovieEntity> = dao.getAllFavorites()
}
