package com.omdbmovies.movieverse.presentation.FavouriteMovieScreen

import MovieAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omdbmovies.movieverse.R
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.databinding.FragmentFavouriteBinding
import com.omdbmovies.movieverse.databinding.FragmentMovieDetailBinding
import com.omdbmovies.movieverse.presentation.HomeScreen.HomeViewModel
import com.omdbmovies.movieverse.presentation.MainActivity
import com.omdbmovies.movieverse.presentation.MovieDetailScreen.SharedMovieViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MovieAdapter
    private val favoriteViewModel:FavoriteViewModel by viewModels()
    private val sharedViewModel: SharedMovieViewModel by activityViewModels()
    private var is_clickedUndo = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).setToolbarTitle("My Favourites")

        adapter = MovieAdapter { selectedMovie ->
            sharedViewModel.clearMovieSelection()
            sharedViewModel.setClickedMovie(selectedMovie)
            findNavController().navigate(R.id.action_favouriteFragment_to_movieDetailFragment)
        }
        binding.favrecyclerViewMovies.adapter = adapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = adapter.currentList[position]

                val favmovies = FavoriteMovieEntity(movie.imdbID,movie.BoxOffice,movie.Director,movie.Genre,movie.Plot,movie.Poster,movie.Title,movie.Year,movie.imdbRating)
                favoriteViewModel.toggleFavorite(favmovies, true)  // true = currently favorite

                Snackbar.make(binding.root, "${movie.Title} removed from favorites", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        is_clickedUndo = true
                        favoriteViewModel.toggleFavorite(favmovies, false)
                    }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.favrecyclerViewMovies)


        favoriteViewModel.favoriteMovies.observe(viewLifecycleOwner) { movieList ->

            val uiList = movieList.map{
                MovieHome(it.imdbRating,it.BoxOffice,it.Director,it.Genre,it.Plot,it.Poster,it.Title,it.Year,it.imdbRating)
            }
            if (uiList.isEmpty()) {
                binding.favrecyclerViewMovies.visibility = View.GONE
                binding.emptyMovieLayout.visibility = View.VISIBLE
            } else {
                binding.favrecyclerViewMovies.visibility = View.VISIBLE
                binding.emptyMovieLayout.visibility = View.GONE
                adapter.submitList(null) {
                    adapter.submitList(uiList)
                }
            }

        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        favoriteViewModel.refreshFavorites()
    }

    }