package com.pseddev.singventory.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pseddev.singventory.BuildConfig
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        SettingsViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            )
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupButtons()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataStatistics.collect { statistics ->
                    updateDataCounts(statistics)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.clearDataEnabled.collect { enabled ->
                    updateClearDataButton(enabled)
                    updateMigrateKeyAdjustmentsButton(enabled)
                }
            }
        }
    }
    
    private fun updateDataCounts(statistics: DataStatistics) {
        with(binding) {
            songsCount.text = "Songs: ${statistics.songsCount}"
            venuesCount.text = "Venues: ${statistics.venuesCount}"
            visitsCount.text = "Visits: ${statistics.visitsCount}"
            performancesCount.text = "Performances: ${statistics.performancesCount}"
            associationsCount.text = "Song-Venue Associations: ${statistics.associationsCount}"
            
            if (statistics.visitFrequencyDays > 0) {
                visitFrequency.text = "Avg visit frequency: Every ${statistics.visitFrequencyDays.roundToInt()} days"
                visitFrequency.visibility = View.VISIBLE
            } else {
                visitFrequency.visibility = View.GONE
            }
            
            // Update app information
            appName.text = "Singventory"
            appBuildVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) - ${BuildConfig.BUILD_TYPE.uppercase()}"
            appOwner.text = "PseudonymousEd" 
            appCopyright.text = "Copyright 2025 - All rights reserved"
        }
    }
    
    
    private fun updateClearDataButton(enabled: Boolean) {
        binding.btnClearData.visibility = if (enabled) View.VISIBLE else View.GONE
    }
    
    private fun updateMigrateKeyAdjustmentsButton(enabled: Boolean) {
        binding.btnMigrateKeyAdjustments.visibility = if (enabled) View.VISIBLE else View.GONE
    }
    
    private fun setupButtons() {
        binding.btnImportExport.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_importExport)
        }
        
        binding.btnPurgeData.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_purgeData)
        }
        
        binding.btnConfiguration.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_configuration)
        }
        
        binding.btnClearData.setOnClickListener {
            showClearDataConfirmation()
        }
        
        binding.btnMigrateKeyAdjustments.setOnClickListener {
            showMigrateKeyAdjustmentsConfirmation()
        }
    }
    
    private fun showClearDataConfirmation() {
        if (!BuildConfig.DEBUG) return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all songs, venues, visits, and performances. This action cannot be undone.\n\nThis is a debug feature for testing purposes only.")
            .setPositiveButton("Clear All Data") { _, _ ->
                showSecondConfirmation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSecondConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Final Confirmation")
            .setMessage("Are you absolutely sure you want to delete ALL data? This will reset the app to a fresh state.")
            .setPositiveButton("DELETE ALL") { _, _ ->
                viewModel.clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showMigrateKeyAdjustmentsConfirmation() {
        if (!BuildConfig.DEBUG) return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Migrate Key Adjustments")
            .setMessage("This will change all existing key adjustments of 0 to 'Unknown' for testing purposes.\n\nThis helps test the new Unknown vs Zero key adjustment feature.\n\nThis action can be undone by manually setting values back to 0.")
            .setPositiveButton("Migrate to Unknown") { _, _ ->
                lifecycleScope.launch {
                    val migratedCount = viewModel.migrateZeroKeyAdjustmentsToUnknown()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Migration Complete")
                        .setMessage("Successfully migrated $migratedCount key adjustment(s) from 0 to Unknown.")
                        .setPositiveButton("OK", null)
                        .show()
                    viewModel.refreshData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to settings screen
        viewModel.refreshData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}