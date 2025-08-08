package com.pseddev.singventory.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pseddev.singventory.databinding.FragmentConfigurationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfigurationFragment : Fragment() {
    
    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "singventory_preferences"
        private const val PREF_DEFAULT_SONG_DURATION = "default_song_duration"
        private const val PREF_AUTO_END_VISITS = "auto_end_visits"
        private const val PREF_SHOW_KEY_REFERENCES = "show_key_references"
        private const val PREF_BACKUP_REMINDERS = "backup_reminders"
        private const val PREF_PERFORMANCE_NOTIFICATIONS = "performance_notifications"
        private const val PREF_VISIT_REMINDERS = "visit_reminders"
        private const val PREF_SONG_SELECTION_DROPDOWN = "song_selection_dropdown"
        
        // Default values
        private const val DEFAULT_SONG_DURATION = 3.5f // minutes
        private const val DEFAULT_AUTO_END_VISITS = true
        private const val DEFAULT_SHOW_KEY_REFERENCES = true
        private const val DEFAULT_BACKUP_REMINDERS = true
        private const val DEFAULT_PERFORMANCE_NOTIFICATIONS = false
        private const val DEFAULT_VISIT_REMINDERS = false
        private const val DEFAULT_SONG_SELECTION_DROPDOWN = false
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupPreferences()
        setupClickListeners()
    }
    
    private fun setupPreferences() {
        // Load current preferences and update UI
        val defaultDuration = preferences.getFloat(PREF_DEFAULT_SONG_DURATION, DEFAULT_SONG_DURATION)
        binding.defaultSongDurationText.text = "${defaultDuration} minutes"
        
        binding.songSelectionDropdownSwitch.isChecked = preferences.getBoolean(PREF_SONG_SELECTION_DROPDOWN, DEFAULT_SONG_SELECTION_DROPDOWN)
        
        binding.autoEndVisitsSwitch.isChecked = preferences.getBoolean(PREF_AUTO_END_VISITS, DEFAULT_AUTO_END_VISITS)
        binding.showKeyReferencesSwitch.isChecked = preferences.getBoolean(PREF_SHOW_KEY_REFERENCES, DEFAULT_SHOW_KEY_REFERENCES)
        binding.backupRemindersSwitch.isChecked = preferences.getBoolean(PREF_BACKUP_REMINDERS, DEFAULT_BACKUP_REMINDERS)
        binding.performanceNotificationsSwitch.isChecked = preferences.getBoolean(PREF_PERFORMANCE_NOTIFICATIONS, DEFAULT_PERFORMANCE_NOTIFICATIONS)
        binding.visitRemindersSwitch.isChecked = preferences.getBoolean(PREF_VISIT_REMINDERS, DEFAULT_VISIT_REMINDERS)
    }
    
    private fun setupClickListeners() {
        binding.defaultSongDurationCard.setOnClickListener {
            showSongDurationDialog()
        }
        
        binding.songSelectionDropdownSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_SONG_SELECTION_DROPDOWN, isChecked).apply()
        }
        
        binding.autoEndVisitsSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_AUTO_END_VISITS, isChecked).apply()
        }
        
        binding.showKeyReferencesSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_SHOW_KEY_REFERENCES, isChecked).apply()
        }
        
        binding.backupRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_BACKUP_REMINDERS, isChecked).apply()
        }
        
        binding.performanceNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_PERFORMANCE_NOTIFICATIONS, isChecked).apply()
        }
        
        binding.visitRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREF_VISIT_REMINDERS, isChecked).apply()
        }
        
        binding.btnResetDefaults.setOnClickListener {
            showResetDefaultsDialog()
        }
    }
    
    private fun showSongDurationDialog() {
        val currentDuration = preferences.getFloat(PREF_DEFAULT_SONG_DURATION, DEFAULT_SONG_DURATION)
        val options = arrayOf("2.5 minutes", "3.0 minutes", "3.5 minutes", "4.0 minutes", "4.5 minutes", "5.0 minutes")
        val values = arrayOf(2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
        
        val currentIndex = values.indexOf(currentDuration).takeIf { it >= 0 } ?: 2
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default Song Duration")
            .setMessage("This is used for estimating total performance time in statistics.")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val selectedDuration = values[which]
                preferences.edit().putFloat(PREF_DEFAULT_SONG_DURATION, selectedDuration).apply()
                binding.defaultSongDurationText.text = "${selectedDuration} minutes"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    private fun showResetDefaultsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset to Defaults")
            .setMessage("This will reset all configuration settings to their default values. Are you sure?")
            .setPositiveButton("Reset") { _, _ ->
                resetToDefaults()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetToDefaults() {
        preferences.edit().clear().apply()
        setupPreferences() // Reload UI with defaults
    }
    
    // Static utility functions for other parts of the app to access preferences
    object ConfigurationManager {
        fun getDefaultSongDuration(context: Context): Float {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getFloat(PREF_DEFAULT_SONG_DURATION, DEFAULT_SONG_DURATION)
        }
        
        fun shouldAutoEndVisits(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_AUTO_END_VISITS, DEFAULT_AUTO_END_VISITS)
        }
        
        fun shouldShowKeyReferences(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SHOW_KEY_REFERENCES, DEFAULT_SHOW_KEY_REFERENCES)
        }
        
        fun shouldShowBackupReminders(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_BACKUP_REMINDERS, DEFAULT_BACKUP_REMINDERS)
        }
        
        fun shouldShowPerformanceNotifications(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_PERFORMANCE_NOTIFICATIONS, DEFAULT_PERFORMANCE_NOTIFICATIONS)
        }
        
        fun shouldShowVisitReminders(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_VISIT_REMINDERS, DEFAULT_VISIT_REMINDERS)
        }
        
        fun shouldUseSongSelectionDropdown(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SONG_SELECTION_DROPDOWN, DEFAULT_SONG_SELECTION_DROPDOWN)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}