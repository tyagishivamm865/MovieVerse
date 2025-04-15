package com.omdbmovies.movieverse.presentation.MovieDetailScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import javax.inject.Inject

class SharedMovieViewModel @Inject constructor() : ViewModel() {
    private val _selectedMovie = MutableLiveData<MoviesResponse?>()
    val selectedMovie: LiveData<MoviesResponse?> = _selectedMovie

    private val _clickedMovie = MutableLiveData<MovieHome?>()
    val clickedMovie: LiveData<MovieHome?> = _clickedMovie

    fun setClickedMovie(movie: MovieHome) {
        _clickedMovie.value = movie
    }

    fun setSelectedMovie(movie: MoviesResponse) {
        _selectedMovie.value = movie
    }

    fun clearMovieSelection() {
        _clickedMovie.value = null
        _selectedMovie.value = null
    }
}