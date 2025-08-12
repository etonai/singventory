package com.pseddev.singventory.ui.venues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.entity.MusicalKey
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentEditSongVenueInfoBinding
import kotlinx.coroutines.launch

class EditSongVenueInfoFragment : Fragment() {
    
    private var _binding: FragmentEditSongVenueInfoBinding? = null
    private val binding get() = _binding!!
    
    // Key options will be built dynamically to include both major and minor keys
    
    private val viewModel: EditSongVenueInfoViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        val songVenueInfoId = arguments?.getLong("songVenueInfoId") ?: -1L
        EditSongVenueInfoViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            songVenueInfoId
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSongVenueInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupVenueKeyDropdown()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupVenueKeyDropdown() {
        val keyOptions = buildList {
            add("")  // Empty option for no key specified
            // Add all major keys first
            addAll(MusicalKey.getMajorKeys().map { it.displayName })
            // Add all minor keys second
            addAll(MusicalKey.getMinorKeys().map { it.displayName })
        }
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, keyOptions)
        binding.venueKeyDropdown.setAdapter(adapter)
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songVenueInfoDetails.collect { details ->
                    details?.let { updateUI(it) }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.btnSaveChanges.isEnabled = !isLoading
                    binding.btnDeleteAssociation.isEnabled = !isLoading
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
        
        binding.btnDeleteAssociation.setOnClickListener {
            showDeleteConfirmation()
        }
        
        // Key adjustment controls
        binding.btnKeyMinus.setOnClickListener {
            adjustKeyValue(-1)
        }
        
        binding.btnKeyPlus.setOnClickListener {
            adjustKeyValue(1)
        }
    }
    
    private fun updateUI(details: EditSongVenueInfoDetails) {
        binding.songTitle.text = details.songName
        binding.artistName.text = details.artistName
        binding.venueName.text = details.venueName
        
        // Display lyrics if available
        if (!details.lyrics.isNullOrBlank()) {
            binding.lyricsSection.visibility = View.VISIBLE
            binding.songLyrics.text = details.lyrics
        } else {
            binding.lyricsSection.visibility = View.GONE
        }
        
        // Set venue song number
        binding.etVenueSongNumber.setText(details.songVenueInfo.venuesSongId ?: "")
        
        // Set venue key in dropdown
        val venueKey = details.songVenueInfo.venueKey ?: ""
        binding.venueKeyDropdown.setText(venueKey, false)
        
        // Set key adjustment
        updateKeyAdjustmentDisplay(details.songVenueInfo.keyAdjustment)
        
        
        // Show performance count
        val performanceText = when (details.performanceCount) {
            0 -> "Never performed at this venue"
            1 -> "1 performance at this venue"
            else -> "${details.performanceCount} performances at this venue"
        }
        binding.performanceCount.text = performanceText
    }
    
    private fun updateKeyAdjustmentDisplay(keyAdjustment: Int) {
        binding.keyAdjustmentValue.text = when {
            SongVenueInfo.isKeyAdjustmentUnknown(keyAdjustment) -> "Unknown"
            keyAdjustment > 0 -> "+$keyAdjustment"
            keyAdjustment < 0 -> keyAdjustment.toString()
            else -> "0"
        }
    }
    
    private fun adjustKeyValue(adjustment: Int) {
        val currentText = binding.keyAdjustmentValue.text.toString()
        val currentValue = when (currentText) {
            "Unknown" -> {
                // First click on Unknown transitions to 0, subsequent clicks adjust from there
                updateKeyAdjustmentDisplay(0)
                return
            }
            "0" -> 0
            else -> currentText.toIntOrNull() ?: 0
        }
        val newValue = (currentValue + adjustment).coerceIn(-12, 12)
        updateKeyAdjustmentDisplay(newValue)
    }
    
    private fun saveChanges() {
        val venueSongNumber = binding.etVenueSongNumber.text.toString().trim().takeIf { it.isNotBlank() }
        val venueKey = binding.venueKeyDropdown.text.toString().trim().takeIf { it.isNotBlank() }
        val keyAdjustmentText = binding.keyAdjustmentValue.text.toString()
        val keyAdjustment = when (keyAdjustmentText) {
            "Unknown" -> SongVenueInfo.UNKNOWN_KEY_ADJUSTMENT
            "0" -> 0
            else -> keyAdjustmentText.toIntOrNull() ?: 0
        }
        viewModel.saveChanges(venueSongNumber, venueKey, keyAdjustment)
        
        // Navigate back
        findNavController().navigateUp()
    }
    
    private fun showDeleteConfirmation() {
        val details = viewModel.songVenueInfoDetails.value ?: return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Association")
            .setMessage("Remove \"${details.songName}\" from \"${details.venueName}\"? This will not delete the song or venue, only their association.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAssociation()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}