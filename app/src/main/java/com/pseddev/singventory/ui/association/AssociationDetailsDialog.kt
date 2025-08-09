package com.pseddev.singventory.ui.association

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pseddev.singventory.data.entity.MusicalKey
import com.pseddev.singventory.data.entity.KeyUtils
import com.pseddev.singventory.databinding.DialogAssociationDetailsBinding

data class AssociationDetails(
    val venueSongId: String? = null,
    val venueKey: String? = null,
    val keyAdjustment: Int = 0
)

class AssociationDetailsDialog : DialogFragment() {
    
    companion object {
        private const val ARG_SONG_NAME = "song_name"
        private const val ARG_VENUE_NAME = "venue_name"
        private const val ARG_EXISTING_VENUE_SONG_ID = "existing_venue_song_id"
        private const val ARG_EXISTING_VENUE_KEY = "existing_venue_key"
        private const val ARG_EXISTING_KEY_ADJUSTMENT = "existing_key_adjustment"
        private const val ARG_SONG_REFERENCE_KEY = "song_reference_key"
        private const val ARG_SONG_PREFERRED_KEY = "song_preferred_key"
        
        private val MUSICAL_KEYS = buildList {
            add("")  // Empty option for no key specified
            // Add all major keys first
            addAll(MusicalKey.getMajorKeys().map { it.displayName })
            // Add all minor keys second
            addAll(MusicalKey.getMinorKeys().map { it.displayName })
        }
        
        fun newInstance(
            songName: String,
            venueName: String,
            existingVenueSongId: String? = null,
            existingVenueKey: String? = null,
            existingKeyAdjustment: Int = 0,
            songReferenceKey: String? = null,
            songPreferredKey: String? = null
        ): AssociationDetailsDialog {
            return AssociationDetailsDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_SONG_NAME, songName)
                    putString(ARG_VENUE_NAME, venueName)
                    existingVenueSongId?.let { putString(ARG_EXISTING_VENUE_SONG_ID, it) }
                    existingVenueKey?.let { putString(ARG_EXISTING_VENUE_KEY, it) }
                    putInt(ARG_EXISTING_KEY_ADJUSTMENT, existingKeyAdjustment)
                    songReferenceKey?.let { putString(ARG_SONG_REFERENCE_KEY, it) }
                    songPreferredKey?.let { putString(ARG_SONG_PREFERRED_KEY, it) }
                }
            }
        }
    }
    
    private var _binding: DialogAssociationDetailsBinding? = null
    private val binding get() = _binding!!
    
    var onDetailsConfirmed: ((AssociationDetails) -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAssociationDetailsBinding.inflate(LayoutInflater.from(requireContext()))
        
        val songName = arguments?.getString(ARG_SONG_NAME) ?: ""
        val venueName = arguments?.getString(ARG_VENUE_NAME) ?: ""
        val existingVenueSongId = arguments?.getString(ARG_EXISTING_VENUE_SONG_ID)
        val existingVenueKey = arguments?.getString(ARG_EXISTING_VENUE_KEY)
        val existingKeyAdjustment = arguments?.getInt(ARG_EXISTING_KEY_ADJUSTMENT, 0) ?: 0
        val songReferenceKey = arguments?.getString(ARG_SONG_REFERENCE_KEY)
        val songPreferredKey = arguments?.getString(ARG_SONG_PREFERRED_KEY)
        
        setupUI(songName, venueName, existingVenueSongId, existingVenueKey, existingKeyAdjustment, songReferenceKey, songPreferredKey)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Association Details")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val details = AssociationDetails(
                    venueSongId = binding.etVenueSongId.text.toString().trim().takeIf { it.isNotBlank() },
                    venueKey = binding.venueKeyDropdown.text.toString().trim().takeIf { it.isNotBlank() },
                    keyAdjustment = getCurrentKeyAdjustment()
                )
                onDetailsConfirmed?.invoke(details)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    private fun setupUI(
        songName: String, 
        venueName: String,
        existingVenueSongId: String?,
        existingVenueKey: String?,
        existingKeyAdjustment: Int,
        songReferenceKey: String?,
        songPreferredKey: String?
    ) {
        // Set dialog title info
        binding.songTitle.text = songName
        binding.venueName.text = venueName
        
        // Setup venue key dropdown
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, MUSICAL_KEYS)
        binding.venueKeyDropdown.setAdapter(adapter)
        
        // Smart auto-population: Use song's preferred key information if venue key is not set
        val shouldAutoPopulate = existingVenueKey.isNullOrBlank()
        
        if (shouldAutoPopulate) {
            // Auto-populate venue key with song's preferred key if available
            songPreferredKey?.let { preferredKeyStr ->
                val preferredKey = KeyUtils.parseKey(preferredKeyStr)
                preferredKey?.let {
                    binding.venueKeyDropdown.setText(it.displayName, false)
                }
            }
            
            // Auto-calculate key adjustment if song has both reference and preferred keys
            if (songReferenceKey != null && songPreferredKey != null) {
                val referenceKey = KeyUtils.parseKey(songReferenceKey)
                val preferredKey = KeyUtils.parseKey(songPreferredKey)
                
                if (referenceKey != null && preferredKey != null) {
                    val adjustment = MusicalKey.calculateKeyAdjustment(referenceKey, preferredKey)
                    updateKeyAdjustmentDisplay(adjustment)
                } else {
                    updateKeyAdjustmentDisplay(0)
                }
            } else {
                updateKeyAdjustmentDisplay(0)
            }
        }
        
        // Always populate existing values if they exist (takes priority over auto-population)
        existingVenueSongId?.let {
            binding.etVenueSongId.setText(it)
        }
        
        // Only set existing venue key if it's not blank (otherwise auto-population took care of it)
        if (!existingVenueKey.isNullOrBlank()) {
            binding.venueKeyDropdown.setText(existingVenueKey, false)
        }
        
        // Only override key adjustment if we have an existing non-zero value
        if (existingKeyAdjustment != 0) {
            updateKeyAdjustmentDisplay(existingKeyAdjustment)
        }
        
        // Setup key adjustment controls
        binding.btnKeyMinus.setOnClickListener {
            adjustKeyValue(-1)
        }
        
        binding.btnKeyPlus.setOnClickListener {
            adjustKeyValue(1)
        }
    }
    
    private fun updateKeyAdjustmentDisplay(keyAdjustment: Int) {
        binding.keyAdjustmentValue.text = when {
            keyAdjustment > 0 -> "+$keyAdjustment"
            keyAdjustment < 0 -> keyAdjustment.toString()
            else -> "0"
        }
    }
    
    private fun adjustKeyValue(adjustment: Int) {
        val currentText = binding.keyAdjustmentValue.text.toString()
        val currentValue = when (currentText) {
            "0" -> 0
            else -> currentText.toIntOrNull() ?: 0
        }
        val newValue = (currentValue + adjustment).coerceIn(-12, 12)
        updateKeyAdjustmentDisplay(newValue)
    }
    
    private fun getCurrentKeyAdjustment(): Int {
        val currentText = binding.keyAdjustmentValue.text.toString()
        return when (currentText) {
            "0" -> 0
            else -> currentText.toIntOrNull() ?: 0
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}