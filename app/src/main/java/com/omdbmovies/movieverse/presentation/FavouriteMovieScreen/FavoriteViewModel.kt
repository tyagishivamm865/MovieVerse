package com.omdbmovies.movieverse.presentation.FavouriteMovieScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import com.omdbmovies.movieverse.data.Repository.FavoriteMovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteMovieRepository
) : ViewModel() {

    private val _favoriteMovies = MutableLiveData<List<FavoriteMovieEntity>>()
    val favoriteMovies: LiveData<List<FavoriteMovieEntity>> = _favoriteMovies

    init {
        refreshFavorites()
    }

    fun toggleFavorite(movie: FavoriteMovieEntity, isFav: Boolean) {
        viewModelScope.launch {
            if (isFav) repository.removeFromFavorites(movie)
            else{
                repository.addToFavorites(movie)
                _favoriteMovies.value = repository.getAllFavorites()
            }

            refreshFavorites()
        }
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            val list = repository.getAllFavorites()
            _favoriteMovies.postValue(list)
        }
    }

     fun isFavorite(id: String): LiveData<Boolean>{
        return repository.isFavoriteLive(id)
    }
}
