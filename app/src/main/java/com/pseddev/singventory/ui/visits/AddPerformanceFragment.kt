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
import com.google.android.material.snackbar.Snackbar
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentAddPerformanceBinding
import com.pseddev.singventory.ui.settings.ConfigurationFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddPerformanceFragment : Fragment() {
    
    private var _binding: FragmentAddPerformanceBinding? = null
    private val binding get() = _binding!!
    
    private val visitId: Long by lazy {
        arguments?.getLong("visitId") ?: -1L
    }
    
    private val preSelectedSongId: Long by lazy {
        arguments?.getLong("preSelectedSongId") ?: -1L
    }
    
    private val viewModel: AddPerformanceViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        AddPerformanceViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            visitId
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
        _binding = FragmentAddPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSongSelection()
        setupPerformanceDetails()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupSongSelection() {
        // Check if we have a pre-selected song
        val hasPreSelectedSong = preSelectedSongId != -1L
        
        // Determine which mode to use based on configuration
        isDropdownMode = ConfigurationFragment.ConfigurationManager.shouldUseSongSelectionDropdown(requireContext())
        
        if (isDropdownMode) {
            setupDropdownMode()
        } else {
            setupSearchMode()
        }
        
        // If we have a pre-selected song, disable selection and pre-populate
        if (hasPreSelectedSong) {
            setupPreSelectedSong()
        }
    }
    
    private fun setupPreSelectedSong() {
        // Disable the song selection inputs
        binding.songSearchInput.isEnabled = false
        binding.songDropdownInput.isEnabled = false
        
        // Load the pre-selected song and populate the field
        lifecycleScope.launch {
            viewModel.getSongById(preSelectedSongId)?.let { song ->
                selectedSong = song
                val songDisplay = formatSongDisplay(song)
                
                if (isDropdownMode) {
                    binding.songDropdownInput.setText(songDisplay, false)
                } else {
                    binding.songSearchInput.setText(songDisplay)
                }
            }
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
            if (isDropdownMode) {
                // In dropdown mode, use songsWithVenueInfoList
                if (position < songsWithVenueInfoList.size) {
                    selectedSong = songsWithVenueInfoList[position].song
                }
            } else {
                // In search mode, use songsList
                if (position < songsList.size) {
                    selectedSong = songsList[position]
                }
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
    
    private fun setupPerformanceDetails() {
        // Initialize performance timestamp to current time
        updateTimestampDisplay()
        
        // Initialize key adjustment
        updateKeyAdjustmentDisplay()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visitDetails.collect { visitDetails ->
                    visitDetails?.let { details ->
                        binding.venueName.text = details.venueName
                        binding.visitDateTime.text = details.formattedDateTime
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
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            Snackbar.make(binding.root, "Performance added successfully!", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            val error = it.exceptionOrNull()?.message ?: "Failed to save performance"
                            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                        }
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
        
        // Key adjustment controls
        binding.btnKeyMinus.setOnClickListener {
            keyAdjustment--
            updateKeyAdjustmentDisplay()
        }
        
        binding.btnKeyPlus.setOnClickListener {
            keyAdjustment++
            updateKeyAdjustmentDisplay()
        }
        
        // Action buttons
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSavePerformance.setOnClickListener {
            savePerformance()
        }
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
    
    private fun updateTimestampDisplay() {
        binding.etPerformanceDateTime.setText(dateTimeFormat.format(Date(performanceTimestamp)))
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
    
    private fun savePerformance() {
        val song = selectedSong
        if (song == null) {
            Snackbar.make(binding.root, "Please select a song", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val notes = binding.etPerformanceNotes.text.toString().trim().takeIf { it.isNotBlank() }
        
        viewModel.savePerformance(
            song = song,
            timestamp = performanceTimestamp,
            keyAdjustment = keyAdjustment,
            notes = notes
        )
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
        val keyAdjustment = songWithVenueInfo.keyAdjustment
        return if (keyAdjustment != null && keyAdjustment != 0) {
            val adjustmentText = if (keyAdjustment > 0) {
                "+$keyAdjustment"
            } else {
                keyAdjustment.toString()
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