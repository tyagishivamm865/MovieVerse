package com.omdbmovies.movieverse.presentation

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.omdbmovies.movieverse.R
import com.omdbmovies.movieverse.databinding.ActivityMainBinding
import com.omdbmovies.movieverse.presentation.FavouriteMovieScreen.FavouriteFragment
import com.omdbmovies.movieverse.presentation.HomeScreen.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbarTitle("Home")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        setupBottomNavigation()
    }

    fun setupBottomNavigation(){
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val navController = findNavController(R.id.nav_host_fragment)

            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.favouriteFragment -> {
                    navController.popBackStack(R.id.favouriteFragment, false)
                    navController.navigate(R.id.favouriteFragment)
                    true
                }

                else -> false
            }

        }
    }
    fun setToolbarTitle(title: String) {
        binding.customToolbar.tvToolbarTitle.text = title

    }
}
