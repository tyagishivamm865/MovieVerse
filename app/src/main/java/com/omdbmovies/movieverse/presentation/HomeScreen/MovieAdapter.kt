import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.omdbmovies.movieverse.R
import com.omdbmovies.movieverse.data.Local.model.MovieHome
import com.omdbmovies.movieverse.databinding.HomeMovieItemBinding
import com.omdbmovies.movieverse.presentation.HomeScreen.MovieDiffCallback

class MovieAdapter(private val itemClickListener: (MovieHome) -> Unit) : ListAdapter<MovieHome, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    inner class MovieViewHolder(private val binding: HomeMovieItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieHome) {
            binding.tvTitle.text = movie.Title
            binding.tvYear.text = movie.Year
            Glide.with(binding.imgPoster.context)
                .load(movie.Poster)
                .transform(RoundedCorners(12))
                .error(R.drawable.movie_placeholder)
                .into(binding.imgPoster)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = HomeMovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            itemClickListener(movie)
        }
    }
}
