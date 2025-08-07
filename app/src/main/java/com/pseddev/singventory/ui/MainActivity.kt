package com.pseddev.singventory.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        // Enable edge-to-edge display (modern Android approach for SDK 35/36)
        enableEdgeToEdge()
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupEdgeToEdgeHandling()
        setupNavigation()
        restoreBottomNavigationState(savedInstanceState)
    }
    
    private fun setupEdgeToEdgeHandling() {
        // Apply window insets to handle system bars properly
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top inset to nav host fragment to avoid drawing under system UI
            binding.navHostFragment.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0 // Don't apply bottom padding here - bottom nav handles it
            )
            
            // Apply bottom inset to bottom navigation
            binding.bottomNavigation.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            )
            
            // Return the insets unchanged for other views
            insets
        }
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