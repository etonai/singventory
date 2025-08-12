package com.pseddev.singventory

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Application class for Singventory with Firebase initialization
 * Based on PlayStreak's proven analytics architecture
 */
class SingventoryApplication : Application() {
    
    // Firebase Analytics instance - accessible globally
    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase Analytics (with error handling for missing config)
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            
            // Enable analytics collection (can be toggled for compliance)
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)
            
            // Set user properties for analytics segmentation
            setUserProperties()
        } catch (e: Exception) {
            // Handle Firebase initialization errors gracefully
            // This prevents crashes when google-services.json is missing or invalid
            println("Firebase Analytics initialization failed: ${e.message}")
        }
    }
    
    /**
     * Set user properties for analytics segmentation
     */
    private fun setUserProperties() {
        try {
            // Set app version for analytics filtering
            firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            
            // Set build type for debug vs release analytics
            firebaseAnalytics.setUserProperty("build_type", BuildConfig.BUILD_TYPE)
        } catch (e: Exception) {
            // Handle errors if Firebase isn't properly initialized
            println("Failed to set user properties: ${e.message}")
        }
    }
    
    /**
     * Enable/disable analytics collection for compliance
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        try {
            firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        } catch (e: Exception) {
            println("Failed to set analytics enabled: ${e.message}")
        }
    }
}