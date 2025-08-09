package com.pseddev.singventory.ui.association

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        
        private val MUSICAL_KEYS = listOf(
            "",  // Empty option for no key specified
            "C",
            "C#/Db",
            "D",
            "D#/Eb", 
            "E",
            "F",
            "F#/Gb",
            "G",
            "G#/Ab",
            "A",
            "A#/Bb",
            "B"
        )
        
        fun newInstance(
            songName: String,
            venueName: String,
            existingVenueSongId: String? = null,
            existingVenueKey: String? = null,
            existingKeyAdjustment: Int = 0
        ): AssociationDetailsDialog {
            return AssociationDetailsDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_SONG_NAME, songName)
                    putString(ARG_VENUE_NAME, venueName)
                    existingVenueSongId?.let { putString(ARG_EXISTING_VENUE_SONG_ID, it) }
                    existingVenueKey?.let { putString(ARG_EXISTING_VENUE_KEY, it) }
                    putInt(ARG_EXISTING_KEY_ADJUSTMENT, existingKeyAdjustment)
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
        
        setupUI(songName, venueName, existingVenueSongId, existingVenueKey, existingKeyAdjustment)
        
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
        existingKeyAdjustment: Int
    ) {
        // Set dialog title info
        binding.songTitle.text = songName
        binding.venueName.text = venueName
        
        // Setup venue key dropdown
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, MUSICAL_KEYS)
        binding.venueKeyDropdown.setAdapter(adapter)
        
        // Populate existing values
        existingVenueSongId?.let {
            binding.etVenueSongId.setText(it)
        }
        
        existingVenueKey?.let {
            binding.venueKeyDropdown.setText(it, false)
        }
        
        updateKeyAdjustmentDisplay(existingKeyAdjustment)
        
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