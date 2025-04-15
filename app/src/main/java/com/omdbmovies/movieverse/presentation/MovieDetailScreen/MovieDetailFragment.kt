package com.omdbmovies.movieverse.presentation.MovieDetailScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.omdbmovies.movieverse.R
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.data.Remote.model.MoviesResponse
import com.omdbmovies.movieverse.databinding.FragmentMovieDetailBinding
import com.omdbmovies.movieverse.presentation.FavouriteMovieScreen.FavoriteViewModel
import com.omdbmovies.movieverse.presentation.HomeScreen.HomeViewModel
import com.omdbmovies.movieverse.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedMovieViewModel by activityViewModels()
    private val favoriteViewModel:FavoriteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setToolbarTitle("Now Showing")


        sharedViewModel.selectedMovie.observe(viewLifecycleOwner) { movie ->
            movie?.let {
                bindMovieData(it)
                setupFavoriteButton(FavoriteMovieEntity(it.imdbRating!!,it.BoxOffice!!,it.Director!!,it.Genre!!,it.Plot!!,it.Poster!!,it.Title!!,it.Year!!,it.imdbRating))
            }
        }

        sharedViewModel.clickedMovie.observe(viewLifecycleOwner) { clickedMovie ->
            clickedMovie?.let {
                bindClickedMovieData(it)
                setupFavoriteButton(FavoriteMovieEntity(it.imdbRating,it.BoxOffice,it.Director,it.Genre,it.Plot,it.Poster,it.Title,it.Year,it.imdbRating))
            }
        }

    }

    private fun bindMovieData(movie: MoviesResponse) {
        binding.tvTitle.text = "Title: ${movie.Title}"
        binding.tvYear.text = "Year: ${movie.Year}"
        binding.tvDirector.text = "Director: ${movie.Director}"
        binding.tvGenre.text = "Genre: ${movie.Genre}"
        binding.tvPlot.text = movie.Plot
        binding.tvImdbRating.text = "IMDb Rating: ${movie.imdbRating}"
        binding.tvBoxOffice.text = "Box Office: ${movie.BoxOffice}"

        Glide.with(this)
            .load(movie.Poster)
            .error(R.drawable.movie_placeholder)
            .into(binding.ivPoster)
    }

    private fun bindClickedMovieData(movie: MovieHome) {
        binding.tvTitle.text = "Title: ${movie.Title}"
        binding.tvYear.text = "Year: ${movie.Year}"
        binding.tvDirector.text = "Director: ${movie.Director}"
        binding.tvGenre.text = "Genre: ${movie.Genre}"
        binding.tvPlot.text = movie.Plot
        binding.tvImdbRating.text = "IMDb Rating: ${movie.imdbRating}"
        binding.tvBoxOffice.text = "Box Office: ${movie.BoxOffice}"

        Glide.with(this)
            .load(movie.Poster)
            .into(binding.ivPoster)
    }


    private fun setupFavoriteButton(movie: FavoriteMovieEntity) {
        var currentFavState = false

        favoriteViewModel.isFavorite(movie.imdbID).observe(viewLifecycleOwner) { isFav ->
            currentFavState = isFav
            binding.ivLike.setImageResource(
                if (isFav) R.drawable.unlike else R.drawable.like
            )
        }

        binding.ivLike.setOnClickListener {
            favoriteViewModel.toggleFavorite(movie, currentFavState)
        }
    }


}