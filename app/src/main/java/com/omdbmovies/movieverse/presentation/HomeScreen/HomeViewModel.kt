package com.omdbmovies.movieverse.presentation.HomeScreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omdbmovies.movieverse.data.Repository.MovieRepository
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import com.omdbmovies.movieverse.data.Local.model.OfflineMovieResponse
import com.omdbmovies.movieverse.utils.Helper
import com.omdbmovies.movieverse.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val _movieLiveData = MutableLiveData<UiState<List<MovieHome>>>()
    val movieLiveData: LiveData<UiState<List<MovieHome>>> = _movieLiveData

    private val _searchedMovieState = MutableLiveData<UiState<List<MoviesResponse?>>>()
    val searchedMovieState: LiveData<UiState<List<MoviesResponse?>>> = _searchedMovieState
    var searchJob: Job? = null
    private var isClearingSearch = false


    init {
        fetchMovies()
        viewModelScope.launch {
            searchQuery
                .debounce(400)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchedMovieState.postValue(UiState.Empty)
                    } else {
                        getMoviesByTitle2(query)
                    }
                }
        }
    }

    fun fetchMovies() {
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val moviesFromDb = repository.getMoviesFromDb()
            if (moviesFromDb.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    if (Helper.isConnected(context)) {
                        fetchMultipleMoviesForHome()
                    } else {
                        _movieLiveData.value =
                            UiState.Error("Network is Unavailable") // Handle error if network is unavailable
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    _movieLiveData.value = UiState.Success(
                        moviesFromDb.map {
                            MovieHome(
                                it.imdbRating,
                                it.BoxOffice,
                                it.Director,
                                it.Genre,
                                it.Plot,
                                it.Poster,
                                it.Title,
                                it.Year,
                                it.imdbRating
                            )
                        }
                    )
                }
            }
        }
    }


    fun fetchMultipleMoviesForHome() {
        val movieTitles = listOf(
            "Inception", "Titanic", "Interstellar", "Joker", "Gladiator",
            "Avatar", "Iron Man", "The Prestige", "Shutter Island", "The Matrix",
            "Up", "Coco", "The Godfather", "Pulp Fiction", "Forrest Gump",
            "Fight Club", "The Dark Knight", "Avengers", "Frozen", "Inside Out", "harry"
        )
        viewModelScope.launch {
            _movieLiveData.value = UiState.Loading
            val randomTitles = movieTitles.shuffled().take(7)

            try {
                val deferredList = randomTitles.map { title ->
                    async(Dispatchers.IO) {
                        try {
                            val response = repository.getMovieByTitle(title)
                            if (response.isSuccessful) {
                                response.body()
                            } else null
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error fetching $title: ${e.message}")
                            null
                        }
                    }
                }

                val movies = deferredList.awaitAll().filterNotNull()
                val offlineMovies = movies.map {
                    OfflineMovieResponse(
                        it.imdbRating!!,
                        it.BoxOffice!!,
                        it.Director!!,
                        it.Genre!!,
                        it.Plot!!,
                        it.Poster!!,
                        it.Title!!,
                        it.Year!!,
                        it.imdbRating
                    )
                }
                val autoSearchMovies = movies.map {
                    MovieHome(
                        it.imdbRating!!,
                        it.BoxOffice!!,
                        it.Director!!,
                        it.Genre!!,
                        it.Plot!!,
                        it.Poster!!,
                        it.Title!!,
                        it.Year!!,
                        it.imdbRating
                    )
                }
                _movieLiveData.value = UiState.Success(autoSearchMovies)
                repository.saveMoviesToDb(offlineMovies)


            } catch (e: Exception) {
                _movieLiveData.value = UiState.Error("Error is ${e.message}")
                Log.e("HomeViewModel", "General error: ${e.message}")
            }
        }
    }

    suspend fun getMoviesByTitle2(query: String) {
        if (isClearingSearch) {
            isClearingSearch = false
            return
        }
        _searchedMovieState.value = UiState.Loading



        try {
            val result = repository.getMovieByTitle(query)

            if (!result.body()?.imdbID.isNullOrEmpty()) {
                if (_searchedMovieState.value is UiState.Empty) {
                    return
                }
                _searchedMovieState.value = UiState.Success(listOf(result.body()))
            } else {
                _searchedMovieState.value = UiState.Error("Data Not Found")
            }

            if (result.body()?.Response == "False" && result.errorBody()?.string()
                    ?.contains("limit") == true
            ) {
                _searchedMovieState.value = UiState.Error("Daily limit reached.")
            }
        } catch (e: HttpException) {
            when (e.code()) {
                429 -> {
                    _searchedMovieState.value =
                        UiState.Error("Rate limit exceeded. Please try again tomorrow.")
                }

                else -> {
                    _searchedMovieState.value = UiState.Error("Server error: ${e.code()}")
                }
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException -> {
                    _searchedMovieState.value =
                        UiState.Error("Request timed out. Please try again.")
                }

                is UnknownHostException -> {
                    _searchedMovieState.value = UiState.Error("No Internet Connection.")
                }

                is IOException -> {
                    _searchedMovieState.value =
                        UiState.Error("Network error. Please check your connection.")
                }

                else -> {
                        _searchedMovieState.value = UiState.Error("Unexpected error occurred")
                }
            }
        }

    }

    fun getMoviesByTitle(query: String) {
        if (query.isBlank()) {
            _searchedMovieState.value = UiState.Empty
        } else {
//            latestQuery = query
            searchQuery.value = ""
            isClearingSearch = false
            searchQuery.value = query
        }
    }

    fun clearSearch() {
//        latestQuery = null
        isClearingSearch = true
        _searchedMovieState.value = UiState.Empty
    }

}
