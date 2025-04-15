package com.omdbmovies.movieverse

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.omdbmovies.movieverse.Utils.getOrAwaitValue
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import com.omdbmovies.movieverse.data.Repository.FavoriteMovieRepository
import com.omdbmovies.movieverse.presentation.FavouriteMovieScreen.FavoriteViewModel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class FavoriteViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FavoriteViewModel

    @MockK
    private lateinit var repository: FavoriteMovieRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        Dispatchers.setMain(testDispatcher)

        viewModel = FavoriteViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `add movie to favorites updates LiveData`() = runTest {
        val movie = FavoriteMovieEntity("1", "Test", "2021", "url")

        coEvery { repository.addToFavorites(movie) } just Runs
        coEvery { repository.getAllFavorites() } returns listOf(movie)

        viewModel.toggleFavorite(movie, isFav = false)

        advanceUntilIdle()

        val result = viewModel.favoriteMovies.getOrAwaitValue()
        assert(result.contains(movie))
    }

    @Test
    fun `remove movie from favorites updates LiveData`() = runTest {
        val movie = FavoriteMovieEntity("1", "Test", "2021", "url")

        coEvery { repository.removeFromFavorites(movie) } just Runs
        coEvery { repository.getAllFavorites() } returns emptyList()

        viewModel.toggleFavorite(movie, isFav = true)

        val result = viewModel.favoriteMovies.getOrAwaitValue()
        assert(!result.contains(movie))
    }


    @Test
    fun `refreshFavorites fetches data from repository`() = runTest {
        val movie = FavoriteMovieEntity("1", "Test", "2021", "url")

        coEvery { repository.getAllFavorites() } returns listOf(movie)

        viewModel.refreshFavorites()
        advanceUntilIdle()
        val result = viewModel.favoriteMovies.getOrAwaitValue()
        assert(result == listOf(movie))
    }

    @Test
    fun `isFavorite returns true when movie is favorite`() {
        val movieId = "1"
        val liveData = MutableLiveData(true)

        every { repository.isFavoriteLive(movieId) } returns liveData

        val result = viewModel.isFavorite(movieId)
        assert(result.value == true)
    }

}
