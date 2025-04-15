package com.omdbmovies.movieverse.data.Local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey
    val imdbID: String = "",
    val BoxOffice: String = "",
    val Director: String = "",
    val Genre: String = "",
    val Plot: String = "",
    val Poster: String = "",
    val Title: String = "",
    val Year: String = "",
    val imdbRating: String = ""
)