package com.omdbmovies.movieverse.presentation.HomeScreen

import androidx.recyclerview.widget.DiffUtil
import com.omdbmovies.movieverse.data.Local.model.MovieHome

class MovieDiffCallback : DiffUtil.ItemCallback<MovieHome>() {
    override fun areItemsTheSame(oldItem: MovieHome, newItem: MovieHome): Boolean {
        return oldItem.Poster == newItem.Poster
    }

    override fun areContentsTheSame(oldItem: MovieHome, newItem: MovieHome): Boolean {
        return oldItem == newItem
    }
}
