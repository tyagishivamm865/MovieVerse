package com.omdbmovies.movieverse

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omdbmovies.movieverse.Utils.getOrAwaitValue
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import com.omdbmovies.movieverse.presentation.MovieDetailScreen.SharedMovieViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class SharedMovieViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SharedMovieViewModel

    @Before
    fun setup() {
        viewModel = SharedMovieViewModel()
    }

    @Test
    fun `setClickedMovie updates LiveData`() {
        val movie = MovieHome("1", "Movie Title", "2024", "posterUrl")
        viewModel.setClickedMovie(movie)

        val result = viewModel.clickedMovie.getOrAwaitValue()
        assert(result == movie)
    }


    @Test
    fun `setSelectedMovie sets selected movie correctly`() {
        val movieResponse = MoviesResponse("1", "Movie Title", "2024", "posterUrl")
        viewModel.setSelectedMovie(movieResponse)

        val result = viewModel.selectedMovie.getOrAwaitValue()
        assert(result == movieResponse)
    }

    @Test
    fun `clearMovieSelection clears both clicked and selected movie`() {
        viewModel.setClickedMovie(MovieHome("1", "Title", "2024", "poster"))
        viewModel.setSelectedMovie(MoviesResponse("1", "Title", "2024", "poster"))

        viewModel.clearMovieSelection()

        assert(viewModel.clickedMovie.getOrAwaitValue() == null)
        assert(viewModel.selectedMovie.getOrAwaitValue() == null)
    }
}
