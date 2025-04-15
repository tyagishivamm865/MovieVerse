package com.omdbmovies.movieverse.data.Local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omdbmovies.movieverse.data.Local.model.FavoriteMovieEntity
import com.omdbmovies.movieverse.data.Local.model.OfflineMovieResponse

@Database(entities = [OfflineMovieResponse::class, FavoriteMovieEntity::class], version = 1)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao

/*    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }*/
}
