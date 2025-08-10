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
        
        // Add custom listener for subpage highlighting without interfering with navigation
        setupSubpageHighlighting(navController)
    }
    
    private fun setupSubpageHighlighting(navController: androidx.navigation.NavController) {
        // Add destination listener that ONLY affects highlighting for subpages
        // Does not interfere with normal navigation behavior
        navController.addOnDestinationChangedListener { _, destination, _ ->
            try {
                if (::binding.isInitialized) {
                    val parentNavigationId = getParentNavigationId(destination.id)
                    if (parentNavigationId != null) {
                        // For subpages, override the bottom navigation selection to show parent
                        // This runs after navigation is complete, so it won't interfere
                        binding.bottomNavigation.menu.findItem(parentNavigationId)?.isChecked = true
                    }
                    // For main pages, let the default behavior handle it (parentNavigationId will be null)
                }
            } catch (e: Exception) {
                // Silently handle exceptions to prevent crashes
            }
        }
    }
    
    private fun getParentNavigationId(destinationId: Int): Int? {
        return when (destinationId) {
            // Songs subpages
            R.id.addSongFragment,
            R.id.editSongFragment,
            R.id.associateVenuesWithSongFragment -> R.id.navigation_songs
            
            // Venues subpages  
            R.id.addVenueFragment,
            R.id.editVenueFragment,
            R.id.venueSongsFragment,
            R.id.associateSongsWithVenueFragment,
            R.id.editSongVenueInfoFragment -> R.id.navigation_venues
            
            // Visits subpages
            R.id.startVisitFragment,
            R.id.activeVisitFragment,
            R.id.visitDetailsFragment,
            R.id.addPerformanceFragment,
            R.id.performanceEditFragment -> R.id.navigation_visits
            
            // Settings subpages
            R.id.importExportFragment,
            R.id.purgeDataFragment,
            R.id.configurationFragment -> R.id.navigation_settings
            
            // Main pages - return null to use default behavior
            R.id.navigation_songs,
            R.id.navigation_venues, 
            R.id.navigation_visits,
            R.id.navigation_settings -> null
            
            // Unknown destinations - return null
            else -> null
        }
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