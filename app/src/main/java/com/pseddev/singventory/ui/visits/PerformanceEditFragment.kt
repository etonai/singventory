package com.pseddev.singventory.ui.visits

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import android.widget.Toast
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentPerformanceEditBinding
import com.pseddev.singventory.ui.settings.ConfigurationFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PerformanceEditFragment : Fragment() {
    
    private var _binding: FragmentPerformanceEditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerformanceEditViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        val performanceId = arguments?.getLong("performanceId") ?: -1L
        PerformanceEditViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            performanceId
        )
    }
    
    private lateinit var songAdapter: ArrayAdapter<String>
    private lateinit var songDropdownAdapter: ArrayAdapter<String>
    private var songsList: List<Song> = emptyList()
    private var songsWithVenueInfoList: List<SongWithVenueInfo> = emptyList()
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    private var isDropdownMode = false
    private var selectedSong: Song? = null
    private var performanceTimestamp: Long = System.currentTimeMillis()
    private var keyAdjustment: Int = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSongSelection()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupSongSelection() {
        // Determine which mode to use based on configuration
        isDropdownMode = ConfigurationFragment.ConfigurationManager.shouldUseSongSelectionDropdown(requireContext())
        
        if (isDropdownMode) {
            setupDropdownMode()
        } else {
            setupSearchMode()
        }
    }
    
    private fun setupSearchMode() {
        binding.searchModeLayout.visibility = View.VISIBLE
        binding.dropdownModeLayout.visibility = View.GONE
        
        // Initialize adapter for search mode
        songAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.songSearchInput.setAdapter(songAdapter)
        binding.songSearchInput.threshold = 1
        
        // Handle song selection from search
        binding.songSearchInput.setOnItemClickListener { _, _, position, _ ->
            if (position < songsList.size) {
                selectedSong = songsList[position]
            }
        }
        
        // Handle text changes for search
        binding.songSearchInput.doOnTextChanged { text, _, _, _ ->
            text?.let { query ->
                viewModel.updateSongSearchQuery(query.toString())
            }
        }
    }
    
    private fun setupDropdownMode() {
        binding.searchModeLayout.visibility = View.GONE
        binding.dropdownModeLayout.visibility = View.VISIBLE
        
        // Initialize adapter for dropdown mode
        songDropdownAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.songDropdownInput.setAdapter(songDropdownAdapter)
        
        // Handle song selection from dropdown
        binding.songDropdownInput.setOnItemClickListener { _, _, position, _ ->
            if (position < songsWithVenueInfoList.size) {
                selectedSong = songsWithVenueInfoList[position].song
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.performanceDetails.collect { details ->
                    details?.let { performanceDetails ->
                        updateUI(performanceDetails)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (isDropdownMode) {
                    viewModel.songsWithVenueInfo.collect { songsWithVenue ->
                        songsWithVenueInfoList = songsWithVenue
                        val songDisplayList = songsWithVenue.map { formatSongDisplayWithKeyAdjustment(it) }
                        songDropdownAdapter.clear()
                        songDropdownAdapter.addAll(songDisplayList)
                        songDropdownAdapter.notifyDataSetChanged()
                    }
                } else {
                    viewModel.filteredSongs.collect { songs ->
                        songsList = songs
                        val songDisplayList = songs.map { formatSongDisplay(it) }
                        songAdapter.clear()
                        songAdapter.addAll(songDisplayList)
                        songAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.btnSaveChanges.isEnabled = !isLoading
                    binding.btnDeletePerformance.isEnabled = !isLoading
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteResult.collect { result ->
                    result?.let { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Performance deleted successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete performance", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.clearDeleteResult()
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        // Date/time picker
        binding.etPerformanceDateTime.setOnClickListener {
            showDateTimePicker()
        }
        
        // Key adjustment buttons
        binding.btnKeyMinus.setOnClickListener {
            keyAdjustment--
            updateKeyAdjustmentDisplay()
        }
        
        binding.btnKeyPlus.setOnClickListener {
            keyAdjustment++
            updateKeyAdjustmentDisplay()
        }
        
        // Action buttons
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
        
        binding.btnDeletePerformance.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun updateUI(details: PerformanceEditData) {
        // Initialize values from performance details
        selectedSong = null // Will be set when we find the matching song
        performanceTimestamp = details.performance.timestamp
        keyAdjustment = details.performance.keyAdjustment
        
        // Set current song in selection UI
        val currentSongDisplay = if (details.artistName.isBlank()) {
            details.songName
        } else {
            "${details.songName} - ${details.artistName}"
        }
        
        if (isDropdownMode) {
            binding.songDropdownInput.setText(currentSongDisplay, false)
        } else {
            binding.songSearchInput.setText(currentSongDisplay)
        }
        
        // Set timestamp
        updateTimestampDisplay()
        
        // Set key adjustment
        updateKeyAdjustmentDisplay()
        
        // Set notes
        binding.etNotes.setText(details.performance.notes ?: "")
    }
    
    private fun updateKeyAdjustmentDisplay() {
        binding.keyAdjustmentValue.text = if (keyAdjustment == 0) {
            "0"
        } else if (keyAdjustment > 0) {
            "+$keyAdjustment"
        } else {
            keyAdjustment.toString()
        }
    }
    
    private fun updateTimestampDisplay() {
        binding.etPerformanceDateTime.setText(dateTimeFormat.format(Date(performanceTimestamp)))
    }
    
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = performanceTimestamp
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        performanceTimestamp = calendar.timeInMillis
                        updateTimestampDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun saveChanges() {
        val notes = binding.etNotes.text.toString().trim().takeIf { it.isNotBlank() }
        
        viewModel.saveChanges(
            song = selectedSong,
            timestamp = performanceTimestamp,
            keyAdjustment = keyAdjustment,
            notes = notes
        )
        
        Snackbar.make(binding.root, "Performance updated successfully!", Snackbar.LENGTH_SHORT).show()
        
        // Navigate back
        findNavController().navigateUp()
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Performance")
            .setMessage("Are you sure you want to delete this performance? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePerformance()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun formatSongDisplay(song: Song): String {
        return if (song.artist.isNullOrBlank()) {
            song.name
        } else {
            "${song.name} - ${song.artist}"
        }
    }
    
    private fun formatSongDisplayWithKeyAdjustment(songWithVenueInfo: SongWithVenueInfo): String {
        val baseDisplay = formatSongDisplay(songWithVenueInfo.song)
        return if (songWithVenueInfo.keyAdjustment != 0 && songWithVenueInfo.keyAdjustment != null) {
            val adjustmentText = if (songWithVenueInfo.keyAdjustment > 0) {
                "+${songWithVenueInfo.keyAdjustment}"
            } else {
                songWithVenueInfo.keyAdjustment.toString()
            }
            "$baseDisplay ($adjustmentText)"
        } else {
            baseDisplay
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}