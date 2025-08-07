package com.pseddev.singventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.pseddev.singventory.R
import com.pseddev.singventory.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    companion object {
        private const val KEY_SELECTED_TAB = "selected_tab"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        restoreBottomNavigationState(savedInstanceState)
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Configure top-level destinations (no up button shown)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_songs,
                R.id.navigation_venues,
                R.id.navigation_visits,
                R.id.navigation_settings
            )
        )
        
        // Connect ActionBar with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Connect BottomNavigationView with NavController
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun restoreBottomNavigationState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            val selectedItemId = bundle.getInt(KEY_SELECTED_TAB, R.id.navigation_songs)
            binding.bottomNavigation.selectedItemId = selectedItemId
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, binding.bottomNavigation.selectedItemId)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}