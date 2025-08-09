package com.pseddev.singventory.ui.visits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentActiveVisitBinding
import com.pseddev.singventory.ui.settings.ConfigurationFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActiveVisitFragment : Fragment() {
    
    private var _binding: FragmentActiveVisitBinding? = null
    private val binding get() = _binding!!
    
    private val visitId: Long by lazy {
        arguments?.getLong("visitId") ?: 1L
    }
    private val viewModel: ActiveVisitViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        ActiveVisitViewModel.Factory(
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
    
    private lateinit var performancesAdapter: PerformancesAdapter
    private lateinit var songAdapter: ArrayAdapter<String>
    private lateinit var songDropdownAdapter: ArrayAdapter<String>
    private var songsList: List<Song> = emptyList()
    private var songsWithVenueInfoList: List<SongWithVenueInfo> = emptyList()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private var isDropdownMode = false
    
    private fun formatSongDisplay(song: Song): String {
        return if (song.artist.isNullOrBlank()) {
            song.name
        } else {
            "${song.name} - ${song.artist}"
        }
    }
    
    private fun formatSongDisplayWithKeyAdjustment(songWithVenueInfo: SongWithVenueInfo): String {
        return viewModel.formatSongDisplayWithKeyAdjustment(songWithVenueInfo)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveVisitBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSongSelection()
        setupPerformancesList()
        setupButtons()
        setupObservers()
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
        binding.songSearchLayout.visibility = View.VISIBLE
        binding.songDropdownLayout.visibility = View.GONE
        
        songAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.songSearchInput.setAdapter(songAdapter)
        binding.songSearchInput.threshold = 1
        
        binding.songSearchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSongSearchQuery(text?.toString() ?: "")
        }
        
        binding.songSearchInput.setOnItemClickListener { _, _, position, _ ->
            val selectedSongName = songAdapter.getItem(position)
            selectedSongName?.let { name ->
                val songWithVenueInfo = songsWithVenueInfoList.find { 
                    formatSongDisplayWithKeyAdjustment(it).equals(name, ignoreCase = true) 
                }
                if (songWithVenueInfo != null) {
                    viewModel.selectSong(songWithVenueInfo.song)
                    showSongInfoDialog(songWithVenueInfo.song, songWithVenueInfo.keyAdjustment)
                }
            }
        }
    }
    
    private fun setupDropdownMode() {
        binding.songSearchLayout.visibility = View.GONE
        binding.songDropdownLayout.visibility = View.VISIBLE
        
        songDropdownAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.songDropdownInput.setAdapter(songDropdownAdapter)
        
        binding.songDropdownInput.setOnItemClickListener { _, _, position, _ ->
            val selectedSongName = songDropdownAdapter.getItem(position)
            selectedSongName?.let { name ->
                val songWithVenueInfo = songsWithVenueInfoList.find { 
                    formatSongDisplayWithKeyAdjustment(it).equals(name, ignoreCase = true) 
                }
                if (songWithVenueInfo != null) {
                    viewModel.selectSong(songWithVenueInfo.song)
                    showSongInfoDialog(songWithVenueInfo.song, songWithVenueInfo.keyAdjustment)
                }
            }
        }
    }
    
    private fun setupPerformancesList() {
        performancesAdapter = PerformancesAdapter { performanceWithSong ->
            // Handle performance click - could show details or edit
            // For now, show a snackbar with details
            Snackbar.make(
                binding.root,
                "Performed ${performanceWithSong.songName} at ${timeFormat.format(Date(performanceWithSong.performance.timestamp))}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        
        binding.performancesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = performancesAdapter
        }
    }
    
    private fun setupButtons() {
        binding.fabQuickLog.setOnClickListener {
            if (isDropdownMode) {
                val selectedText = binding.songDropdownInput.text?.toString()?.trim()
                if (selectedText.isNullOrEmpty()) {
                    showQuickSongDialog()
                } else {
                    // Check if selection matches a song
                    val songWithVenueInfo = songsWithVenueInfoList.find { formatSongDisplayWithKeyAdjustment(it).equals(selectedText, ignoreCase = true) }
                    if (songWithVenueInfo != null) {
                        viewModel.selectSong(songWithVenueInfo.song)
                        showSongInfoDialog(songWithVenueInfo.song, songWithVenueInfo.keyAdjustment)
                    } else {
                        showQuickSongDialog()
                    }
                }
            } else {
                val searchText = binding.songSearchInput.text?.toString()?.trim()
                if (searchText.isNullOrEmpty()) {
                    showQuickSongDialog()
                } else {
                    // Try to find or create song based on search
                    handleQuickLog(searchText)
                }
            }
        }
        
        binding.btnAddQuickNote.setOnClickListener {
            showAddNoteDialog()
        }
        
        binding.btnEndVisit.setOnClickListener {
            showEndVisitDialog()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visit.collect { visit ->
                    visit?.let { v ->
                        val startTime = Date(v.timestamp)
                        binding.visitStartTime.text = "Started: ${timeFormat.format(startTime)}"
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.venue.collect { venue ->
                    venue?.let { v ->
                        binding.venueName.text = v.name
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songsWithVenueInfo.collect { songsWithVenueInfo ->
                    songsWithVenueInfoList = songsWithVenueInfo
                    songsList = songsWithVenueInfo.map { it.song }
                    val songDisplayNames = songsWithVenueInfo.map { formatSongDisplayWithKeyAdjustment(it) }.sorted()
                    
                    if (isDropdownMode) {
                        songDropdownAdapter.clear()
                        songDropdownAdapter.addAll(songDisplayNames)
                        songDropdownAdapter.notifyDataSetChanged()
                    } else {
                        songAdapter.clear()
                        songAdapter.addAll(songDisplayNames)
                        songAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.performances.collect { performances ->
                    performancesAdapter.submitList(performances)
                    
                    // Update performance count
                    val countText = if (performances.size == 1) "1 song" else "${performances.size} songs"
                    binding.performanceCount.text = countText
                    
                    // Show/hide empty view
                    if (performances.isEmpty()) {
                        binding.performancesRecyclerView.visibility = View.GONE
                        binding.emptyPerformancesView.visibility = View.VISIBLE
                    } else {
                        binding.performancesRecyclerView.visibility = View.VISIBLE
                        binding.emptyPerformancesView.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun handleQuickLog(searchText: String) {
        lifecycleScope.launch {
            // Try to find existing song
            val existingSong = songsList.find { 
                it.name.contains(searchText, ignoreCase = true) ||
                formatSongDisplay(it).contains(searchText, ignoreCase = true)
            }
            
            if (existingSong != null) {
                // Find the venue info for this song to get key adjustment
                val songWithVenueInfo = songsWithVenueInfoList.find { it.song.id == existingSong.id }
                viewModel.selectSong(existingSong)
                showSongInfoDialog(existingSong, songWithVenueInfo?.keyAdjustment)
            } else {
                // Show create new song dialog
                showCreateSongDialog(searchText)
            }
        }
    }
    
    private fun showQuickSongDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quick Song Entry")
            .setMessage("What song did you perform?")
            .setView(R.layout.dialog_quick_song)
            .setPositiveButton("Log Performance") { dialog, _ ->
                val dialogView = (dialog as androidx.appcompat.app.AlertDialog).findViewById<View>(R.id.dialog_content)
                val songInput = dialogView?.findViewById<TextInputEditText>(R.id.song_name_input)
                val artistInput = dialogView?.findViewById<TextInputEditText>(R.id.artist_input)
                
                val songName = songInput?.text?.toString()?.trim()
                val artistName = artistInput?.text?.toString()?.trim()
                
                if (!songName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val success = viewModel.createSongAndLog(songName, artistName)
                        if (success) {
                            Snackbar.make(binding.root, "Performance logged!", Snackbar.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(binding.root, "Error logging performance", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCreateSongDialog(searchText: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_quick_song, null)
        val songInput = dialogView.findViewById<TextInputEditText>(R.id.song_name_input)
        
        songInput.setText(searchText)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Song")
            .setMessage("Song not found. Create it now?")
            .setView(dialogView)
            .setPositiveButton("Create & Log") { _, _ ->
                val songName = songInput.text?.toString()?.trim()
                val artistInput = dialogView.findViewById<TextInputEditText>(R.id.artist_input)
                val artistName = artistInput?.text?.toString()?.trim()
                
                if (!songName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val success = viewModel.createSongAndLog(songName, artistName)
                        if (success) {
                            if (isDropdownMode) {
                                binding.songDropdownInput.text?.clear()
                                binding.songDropdownInput.requestFocus()
                            } else {
                                binding.songSearchInput.text?.clear()
                                binding.songSearchInput.requestFocus()
                            }
                            Snackbar.make(binding.root, "Song created and logged!", Snackbar.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(binding.root, "Error creating song", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSongInfoDialog(song: Song, venueKeyAdjustment: Int? = null) {
        val currentVenue = viewModel.venue.value
        
        lifecycleScope.launch {
            // Get venue song information first
            val venueInfo = if (currentVenue != null) {
                viewModel.getSongVenueInfo(song.id, currentVenue.id)
            } else null
            
            // Create comprehensive song information display
            val songInfo = buildString {
                append("Song: ${song.name}\n")
                if (!song.artist.isNullOrBlank()) {
                    append("Artist: ${song.artist}\n")
                }
                
                // Display venue song number if available
                if (!venueInfo?.venuesSongId.isNullOrBlank()) {
                    append("Venue Song #: ${venueInfo?.venuesSongId}\n")
                }
                append("\n")
                
                // Key information
                append("Key Information:\n")
                if (!song.referenceKey.isNullOrBlank()) {
                    append("Reference Key: ${song.referenceKey}\n")
                }
                if (!song.preferredKey.isNullOrBlank()) {
                    append("Preferred Key: ${song.preferredKey}\n")
                }
                if (venueKeyAdjustment != null && venueKeyAdjustment != 0) {
                    val adjustmentString = if (venueKeyAdjustment > 0) "+$venueKeyAdjustment" else venueKeyAdjustment.toString()
                    append("Venue Adjustment: $adjustmentString steps")
                } else if (song.referenceKey == null && song.preferredKey == null) {
                    append("No key information available")
                }
            }
            
            // Show dialog on main thread
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Song Info")
                .setMessage(songInfo)
            .setPositiveButton("Just Sang It!") { _, _ ->
                viewModel.logPerformance()
                if (isDropdownMode) {
                    binding.songDropdownInput.text?.clear()
                    binding.songDropdownInput.requestFocus()
                } else {
                    binding.songSearchInput.text?.clear()
                    binding.songSearchInput.requestFocus()
                }
                Snackbar.make(binding.root, "Performance logged!", Snackbar.LENGTH_SHORT).show()
            }
            .setNeutralButton("With Notes...") { _, _ ->
                showDetailedSongInfoDialog(song, venueKeyAdjustment)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.clearSelectedSong()
            }
            .show()
        }
    }
    
    private fun showDetailedSongInfoDialog(song: Song, venueKeyAdjustment: Int? = null) {
        val currentVenue = viewModel.venue.value
        
        lifecycleScope.launch {
            // Get venue song information first
            val venueInfo = if (currentVenue != null) {
                viewModel.getSongVenueInfo(song.id, currentVenue.id)
            } else null
            
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_performance, null)
            
            // Setup key adjustment controls
            val keyAdjustmentValue = dialogView.findViewById<TextView>(R.id.keyAdjustmentValue)
            val btnKeyMinus = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnKeyMinus)
            val btnKeyPlus = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnKeyPlus)
            val notesInput = dialogView.findViewById<TextInputEditText>(R.id.performance_notes_input)
            
            var currentKeyAdjustment = 0
            
            fun updateKeyAdjustmentDisplay(keyAdjustment: Int) {
                keyAdjustmentValue.text = when {
                    keyAdjustment > 0 -> "+$keyAdjustment"
                    keyAdjustment < 0 -> keyAdjustment.toString()
                    else -> "0"
                }
            }
            
            btnKeyMinus.setOnClickListener {
                currentKeyAdjustment = (currentKeyAdjustment - 1).coerceIn(-12, 12)
                updateKeyAdjustmentDisplay(currentKeyAdjustment)
            }
            
            btnKeyPlus.setOnClickListener {
                currentKeyAdjustment = (currentKeyAdjustment + 1).coerceIn(-12, 12)
                updateKeyAdjustmentDisplay(currentKeyAdjustment)
            }
            
            // Initialize display
            updateKeyAdjustmentDisplay(currentKeyAdjustment)
            
            // Create comprehensive song information display for detailed dialog
            val songInfo = buildString {
                append("Song: ${song.name}\n")
                if (!song.artist.isNullOrBlank()) {
                    append("Artist: ${song.artist}\n")
                }
                
                // Display venue song number if available
                if (!venueInfo?.venuesSongId.isNullOrBlank()) {
                    append("Venue Song #: ${venueInfo?.venuesSongId}\n")
                }
                append("\n")
                
                // Key information
                append("Key Information:\n")
                if (!song.referenceKey.isNullOrBlank()) {
                    append("Reference Key: ${song.referenceKey}\n")
                }
                if (!song.preferredKey.isNullOrBlank()) {
                    append("Preferred Key: ${song.preferredKey}\n")
                }
                if (venueKeyAdjustment != null && venueKeyAdjustment != 0) {
                    val adjustmentString = if (venueKeyAdjustment > 0) "+$venueKeyAdjustment" else venueKeyAdjustment.toString()
                    append("Venue Adjustment: $adjustmentString steps\n")
                }
                append("\nAdjust the key and add notes for this performance:")
            }
            
            // Show dialog on main thread
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Song Info")
                .setMessage(songInfo)
            .setView(dialogView)
            .setPositiveButton("Log Performance") { _, _ ->
                // Get key adjustment and notes from dialog
                val notes = notesInput.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
                viewModel.logPerformance(keyAdjustment = currentKeyAdjustment, notes = notes)
                if (isDropdownMode) {
                    binding.songDropdownInput.text?.clear()
                    binding.songDropdownInput.requestFocus()
                } else {
                    binding.songSearchInput.text?.clear()
                    binding.songSearchInput.requestFocus()
                }
                Snackbar.make(binding.root, "Performance logged with details!", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
        }
    }
    
    private fun showAddNoteDialog() {
        // Implementation for adding visit notes
        Snackbar.make(binding.root, "Add Note feature coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showEndVisitDialog() {
        val currentVisit = viewModel.visit.value
        if (currentVisit == null) return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("End Visit")
            .setMessage("Are you sure you want to end your visit to ${viewModel.venue.value?.name}?")
            .setPositiveButton("End Visit") { _, _ ->
                viewModel.endVisit()
                Snackbar.make(binding.root, "Visit ended!", Snackbar.LENGTH_SHORT).show()
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