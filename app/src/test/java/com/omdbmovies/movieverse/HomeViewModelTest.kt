package com.omdbmovies.movieverse

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omdbmovies.movieverse.Utils.getOrAwaitValue
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import com.omdbmovies.movieverse.data.Repository.MovieRepository
import com.omdbmovies.movieverse.presentation.HomeScreen.HomeViewModel
import com.omdbmovies.movieverse.utils.Helper
import com.omdbmovies.movieverse.utils.UiState
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    @MockK
    private lateinit var repository: MovieRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        context = mockk()
        mockkObject(Helper)
        every { Helper.isConnected(any()) } returns false
        every { context.getSystemService(any()) } returns true
        coEvery { repository.getMoviesFromDb() } returns emptyList()
        viewModel = HomeViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(repository, context)
        viewModel.clearSearch()
    }

    @Test
    fun getMoviesByTitle_returns_success_when_valid_movie_is_found() = runTest {
        val movieResponse = MoviesResponse(imdbID = "tt123456", Title = "Test Movie", Response = "True")
        coEvery { repository.getMovieByTitle("Test Movie") } returns Response.success(movieResponse)

        viewModel.getMoviesByTitle("Test Movie")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()

        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Success)
        assert((result as UiState.Success).data.first()?.Title == "Test Movie")
    }

  @Test
    fun getMoviesByTitle_returns_error_when_movie_not_found() = runTest {
        val movieResponse = MoviesResponse(imdbID = null, Title = null, Response = "False")
        coEvery { repository.getMovieByTitle("Unknown") } returns Response.success(movieResponse)

        viewModel.getMoviesByTitle("Unknown")

      advanceTimeBy(600) // Pass debounce delay
      advanceUntilIdle()

        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error)
    }

      @Test
      fun getMoviesByTitle_handles_timeout_exception() = runTest {
          coEvery { repository.getMovieByTitle(any()) } throws SocketTimeoutException()

          viewModel.getMoviesByTitle("Test")
          advanceTimeBy(600) // Pass debounce delay
          advanceUntilIdle()
          val result = viewModel.searchedMovieState.getOrAwaitValue()
          assert(result is UiState.Error)
          assert((result as UiState.Error).message.contains("Request timed out"))
      }


         @Test
         fun getMoviesByTitle_handles_no_internet_connection() = runTest {
             coEvery { repository.getMovieByTitle(any()) } throws UnknownHostException()

             viewModel.getMoviesByTitle("Test")
             advanceTimeBy(600) // Pass debounce delay
             advanceUntilIdle()
             val result = viewModel.searchedMovieState.getOrAwaitValue()
             assert(result is UiState.Error)
             assert((result as UiState.Error).message.contains("No Internet"))
         }


         @Test
         fun getMoviesByTitle_handles_rate_limiting_with_429_error() = runTest {
             coEvery { repository.getMovieByTitle(any()) } throws HttpException(Response.error<Any>(429, "".toResponseBody()))

             viewModel.getMoviesByTitle("Test")
             advanceTimeBy(600) // Pass debounce delay
             advanceUntilIdle()
             val result = viewModel.searchedMovieState.getOrAwaitValue()
             assert(result is UiState.Error)
             assert((result as UiState.Error).message.contains("Rate limit exceeded"))
         }

        @Test
        fun clearSearch_cancels_search_job_and_sets_state_to_empty() = runTest {
            viewModel.clearSearch()
            advanceTimeBy(600) // Pass debounce delay
            advanceUntilIdle()
            val result = viewModel.searchedMovieState.getOrAwaitValue()
            assert(result is UiState.Empty)
        }

    @Test
    fun getMoviesByTitle_withEmptyQuery_returnsEarlyOrError() = runTest {
        viewModel.getMoviesByTitle("")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()
        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error || result is UiState.Empty)
    }

    @Test
    fun testDebouncedSearch() = runTest {
        coEvery { repository.getMovieByTitle("Spider-Man") } returns Response.success(
            MoviesResponse(
                Title = "Spider-Man",
                imdbID = "tt1234567",
                Poster = "poster.jpg",
                Year = "2002",
                Plot = "Peter Parker becomes Spider-Man",
                Genre = "Action",
                Director = "Sam Raimi",
                BoxOffice = "$800M",
                imdbRating = "7.5",
                Response = "True"
            )
        )
        viewModel.getMoviesByTitle("Spider")
        advanceTimeBy(200) // Not enough to trigger
        viewModel.getMoviesByTitle("Spider-Man")
        advanceTimeBy(500)
        runCurrent()  // This triggers the API call
        val state = viewModel.searchedMovieState.getOrAwaitValue(5000)
        println("State received: $state")
        assertTrue(state is UiState.Success)
    }

    @Test
    fun getMoviesByTitle_cancelsPreviousJobIfCalledAgainQuickly() = runTest {
        // Arrange
        coEvery { repository.getMovieByTitle(any()) } coAnswers {
            delay(200)
            Response.success(MoviesResponse(imdbID = "tt123", Title = "Test4", Response = "True"))
        }

        viewModel.getMoviesByTitle("Test")
        advanceTimeBy(10)
        viewModel.getMoviesByTitle("Test4")

        advanceUntilIdle()

        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Success)
        assert((result as UiState.Success).data.first()?.Title == "Test4")
    }



    @Test
    fun getMoviesByTitle_handles_general_http_error() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "Internal Server Error".toResponseBody()))
        coEvery { repository.getMovieByTitle(any()) } throws httpException

        viewModel.getMoviesByTitle("Test")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()
        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error)
        assert((result as UiState.Error).message.contains("Server error: 500"))
    }

    @Test
    fun getMoviesByTitle_handles_null_body_in_success_response() = runTest {
        coEvery { repository.getMovieByTitle(any()) } returns Response.success(null)

        viewModel.getMoviesByTitle("Test")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()
        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error)
        assert((result as UiState.Error).message.contains("Data Not Found"))
    }

    @Test
    fun getMoviesByTitle_handles_io_exception() = runTest {
        coEvery { repository.getMovieByTitle(any()) } throws java.io.IOException()

        viewModel.getMoviesByTitle("Test")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()
        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error)
        assert((result as UiState.Error).message.contains("Network error"))
    }

    @Test
    fun getMoviesByTitle_handles_unexpected_exception() = runTest {
        coEvery { repository.getMovieByTitle(any()) } throws RuntimeException("Something went wrong")

        viewModel.getMoviesByTitle("Test")
        advanceTimeBy(600) // Pass debounce delay
        advanceUntilIdle()
        val result = viewModel.searchedMovieState.getOrAwaitValue()
        assert(result is UiState.Error)
        assert((result as UiState.Error).message.contains("Unexpected error"))
    }

}
