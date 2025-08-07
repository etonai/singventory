package com.pseddev.singventory.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pseddev.singventory.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val KEY_SONGS_COUNT = "songs_count"
        private const val KEY_VENUES_COUNT = "venues_count"
        private const val KEY_VISITS_COUNT = "visits_count"
        private const val KEY_PERFORMANCES_COUNT = "performances_count"
    }
    
    private var songsCount: Int = 0
    private var venuesCount: Int = 0
    private var visitsCount: Int = 0
    private var performancesCount: Int = 0
    
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
        
        restoreState(savedInstanceState)
        setupDataCounts()
        setupButtons()
    }
    
    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            songsCount = bundle.getInt(KEY_SONGS_COUNT, 0)
            venuesCount = bundle.getInt(KEY_VENUES_COUNT, 0)
            visitsCount = bundle.getInt(KEY_VISITS_COUNT, 0)
            performancesCount = bundle.getInt(KEY_PERFORMANCES_COUNT, 0)
        }
    }
    
    private fun setupDataCounts() {
        // Data counts will be updated with actual repository data in future phases
        // For now, showing saved or placeholder counts
        binding.songsCount.text = "Songs: $songsCount"
        binding.venuesCount.text = "Venues: $venuesCount"
        binding.visitsCount.text = "Visits: $visitsCount"
        binding.performancesCount.text = "Performances: $performancesCount"
    }
    
    private fun setupButtons() {
        binding.btnImportExport.setOnClickListener {
            // Navigate to ImportExportFragment in future phase
            // Following PlayStreak pattern
        }
        
        binding.btnPurgeData.setOnClickListener {
            // Navigate to PurgeDataFragment in future phase
            // Following PlayStreak pattern with stats preservation
        }
        
        binding.btnConfiguration.setOnClickListener {
            // Navigate to ConfigurationFragment in future phase
            // Following PlayStreak pattern
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save data counts to preserve them during configuration changes
        outState.putInt(KEY_SONGS_COUNT, songsCount)
        outState.putInt(KEY_VENUES_COUNT, venuesCount)
        outState.putInt(KEY_VISITS_COUNT, visitsCount)
        outState.putInt(KEY_PERFORMANCES_COUNT, performancesCount)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}