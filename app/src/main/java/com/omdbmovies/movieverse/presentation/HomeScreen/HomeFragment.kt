package com.omdbmovies.movieverse.presentation.HomeScreen

import MovieAdapter
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.omdbmovies.movieverse.R
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import com.omdbmovies.movieverse.databinding.FragmentHomeBinding
import com.omdbmovies.movieverse.presentation.MainActivity
import com.omdbmovies.movieverse.presentation.MovieDetailScreen.SharedMovieViewModel
import com.omdbmovies.movieverse.utils.Helper
import com.omdbmovies.movieverse.utils.NetworkMonitor
import com.omdbmovies.movieverse.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var searched_data: MoviesResponse
    private val sharedViewModel: SharedMovieViewModel by activityViewModels()
    private lateinit var networkMonitor: NetworkMonitor
    private var lastSearchFailedDueToNetwork = false
    private var lastQuery: String? = null
    private var _firsttimecheckinternet = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Initialize NetworkMonitor here to ensure the context is valid
        networkMonitor = NetworkMonitor(context)
    }

    override fun onStart() {
        super.onStart()

        networkMonitor.registerNetworkCallback {
            if (
                isAdded &&
                ::binding.isInitialized &&
                binding.root.isAttachedToWindow &&
                lastSearchFailedDueToNetwork &&
                !lastQuery.isNullOrEmpty()
            ) {
                Snackbar.make(binding.root, "You're back online", Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        hideNoInternetLayout()
                        viewModel.getMoviesByTitle(lastQuery!!)
                        lastSearchFailedDueToNetwork = false
                    }
                    .show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        (requireActivity() as MainActivity).setToolbarTitle("Home")

        setupRecyclerView()
        setupSearch()
        observeMovies()
        binding.btnRetry.setOnClickListener {
            if(Helper.isConnected(requireContext())) {
                hideNoInternetLayout()

                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.getMoviesByTitle(query)
                }
                if(!_firsttimecheckinternet){
                    _firsttimecheckinternet=true
                    viewModel.fetchMovies()
                }
            } else {
                showToast("No Internet Connection. Please Try Again Later")
            }
        }

        binding.searchedMovieCard.setOnClickListener{
            sharedViewModel.clearMovieSelection()
            sharedViewModel.setSelectedMovie(searched_data)
            findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment)
        }

    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { selectedMovie ->
            sharedViewModel.clearMovieSelection()
            sharedViewModel.setClickedMovie(selectedMovie)
            findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment)
        }
        binding.recyclerViewMovies.apply {
            adapter = movieAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeMovies() {
        viewModel.movieLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading(binding.offlineshimmer)
                is UiState.Success -> {
                    hideLoading(binding.offlineshimmer)
                    showRecyclerView()
                    movieAdapter.submitList(state.data)
                }
                is UiState.Error -> {
                    hideLoading(binding.offlineshimmer)
                    _firsttimecheckinternet = false
                    showNoInternetLayout()
                    binding.tvNoInternet.text = state.message
                    showToast(state.message)
                }
                else -> {
                    hideLoading(binding.offlineshimmer)
                    showNotFound()
                }
            }
        }

        viewModel.searchedMovieState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading(binding.shimmer)
                is UiState.Success -> {
                    hideLoading(binding.shimmer)
                    val movie = state.data[0]
                    if (movie != null) {
                        searched_data = movie
                        binding.tvTitle.text = movie.Title
                        binding.tvYear.text = movie.Year
                        loadImage(movie.Poster!!, binding.imgPoster)
                        showSearchedCard()
                    }
                }
                is UiState.Empty -> {
                    hideLoading(binding.shimmer)
                    showRecyclerView()
                }
                is UiState.Error -> {
                    hideLoading(binding.shimmer)
                    if (state.message == "Data Not Found") {
                        showNotFound()
                    } else {
                        showNoInternetLayout()
                        binding.tvNoInternet.text = state.message
                        showToast(state.message)
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        binding.searchInputLayout.isEndIconVisible = false

        binding.etSearch.setOnEditorActionListener { textView, actionId, event ->
            val query = textView.text.toString().trim()
            val shouldSearch = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (shouldSearch) {
                if (Helper.isConnected(requireContext())) {
                    hideNoInternetLayout()
                    if (query.isNotEmpty()) {
                        viewModel.getMoviesByTitle(query)
                        lastSearchFailedDueToNetwork = false
                        lastQuery = query
                    } else {
                        showSnackbar("Search field cannot be empty.")
                        clearSearch()
                    }
                } else {
                    showNoInternetLayout()
                    lastSearchFailedDueToNetwork = true
                    lastQuery = query
                    return@setOnEditorActionListener true
                }

                hideKeyboard()
                true
            } else false
        }

        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString()
            binding.searchInputLayout.isEndIconVisible = query.isNotEmpty()
            if (query.isEmpty()) hideNoInternetLayout()
        }

        binding.searchInputLayout.setEndIconOnClickListener {
            clearSearch()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun showNoInternetLayout() {
        binding.noInternetLayout.visibility = View.VISIBLE
        binding.recyclerViewMovies.visibility = View.GONE
        binding.searchedMovieCard.visibility = View.GONE
    }

    private fun hideNoInternetLayout() {
        binding.noInternetLayout.visibility = View.GONE
        binding.recyclerViewMovies.visibility = View.VISIBLE
    }

    private fun clearSearch() {
        binding.searchInputLayout.isEndIconVisible = false
        binding.etSearch.setText("")
        viewModel.clearSearch()
        hideKeyboard()
    }

    private fun showSnackbar(message: String, action: String? = null, onClick: (() -> Unit)? = null) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            if (action != null && onClick != null) {
                setAction(action) { onClick() }
            }
        }.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(view: View) {
        view.visibility = View.VISIBLE
        if (view is com.facebook.shimmer.ShimmerFrameLayout) view.startShimmer()
        binding.recyclerViewMovies.visibility = View.GONE
        binding.searchedMovieCard.visibility = View.GONE
        binding.notFoundLayout.visibility = View.GONE
    }

    private fun hideLoading(view: View) {
        view.visibility = View.GONE
        if (view is com.facebook.shimmer.ShimmerFrameLayout) view.stopShimmer()
    }

    private fun showRecyclerView() {
        binding.recyclerViewMovies.visibility = View.VISIBLE
        binding.searchedMovieCard.visibility = View.GONE
        binding.notFoundLayout.visibility = View.GONE
    }

    private fun showSearchedCard() {
        binding.searchedMovieCard.visibility = View.VISIBLE
        binding.recyclerViewMovies.visibility = View.GONE
        binding.notFoundLayout.visibility = View.GONE
    }

    private fun showNotFound() {
        binding.notFoundLayout.visibility = View.VISIBLE
        binding.searchedMovieCard.visibility = View.GONE
        binding.recyclerViewMovies.visibility = View.GONE
    }

    private fun loadImage(url: String, target: View) {
        Glide.with(requireContext())
            .load(url)
            .error(R.drawable.movie_placeholder)
            .into(target as android.widget.ImageView)
    }


}
