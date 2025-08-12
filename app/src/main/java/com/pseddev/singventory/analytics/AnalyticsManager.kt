package com.pseddev.singventory.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.pseddev.singventory.BuildConfig
import com.pseddev.singventory.SingventoryApplication

/**
 * Centralized analytics manager for tracking user events in Singventory
 * Based on PlayStreak's proven analytics architecture
 */
class AnalyticsManager(private val context: Context) {
    
    // Analytics toggle for development/testing
    private val analyticsEnabled = !BuildConfig.DEBUG
    
    private val firebaseAnalytics: FirebaseAnalytics? by lazy {
        try {
            (context.applicationContext as SingventoryApplication).firebaseAnalytics
        } catch (e: Exception) {
            null // Return null if Firebase isn't initialized
        }
    }
    
    companion object {
        // Event names - following Firebase naming conventions
        private const val EVENT_NAVIGATION_USED = "navigation_used"
        
        // Parameter names - following Firebase conventions
        private const val PARAM_NAVIGATION_SOURCE = "navigation_source"
        private const val PARAM_NAVIGATION_TARGET = "navigation_target"
    }
    
    /**
     * Track when user uses navigation buttons (Phase 2 & 3 features)
     */
    fun trackNavigationUsed(
        source: String, // "edit_venue", "edit_song"
        target: String  // "venue_songs", "associate_venues"
    ) {
        if (!analyticsEnabled) return
        
        try {
            firebaseAnalytics?.logEvent(EVENT_NAVIGATION_USED) {
                param(PARAM_NAVIGATION_SOURCE, source)
                param(PARAM_NAVIGATION_TARGET, target)
            }
        } catch (e: Exception) {
            // Silently handle analytics errors to prevent app crashes
        }
    }
    
    // Placeholder methods for future implementation
    fun trackSongCreated(hasLyrics: Boolean = false, hasReferenceKey: Boolean = false, hasPreferredKey: Boolean = false) {
        // Implementation will be added when Firebase is properly configured
    }
    
    fun trackSongEdited(hasLyrics: Boolean = false, hasReferenceKey: Boolean = false, hasPreferredKey: Boolean = false) {
        // Implementation will be added when Firebase is properly configured
    }
    
    fun trackVenueCreated(hasAddress: Boolean = false, roomType: String? = null) {
        // Implementation will be added when Firebase is properly configured
    }
    
    fun trackVenueEdited(hasAddress: Boolean = false, roomType: String? = null) {
        // Implementation will be added when Firebase is properly configured
    }
}