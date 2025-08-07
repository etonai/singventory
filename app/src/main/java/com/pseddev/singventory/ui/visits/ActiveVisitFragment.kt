package com.pseddev.singventory.ui.visits

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
    private var songsList: List<Song> = emptyList()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
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
        
        setupSongAutoComplete()
        setupPerformancesList()
        setupButtons()
        setupObservers()
    }
    
    private fun setupSongAutoComplete() {
        songAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.songSearchInput.setAdapter(songAdapter)
        binding.songSearchInput.threshold = 1
        
        binding.songSearchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSongSearchQuery(text?.toString() ?: "")
        }
        
        binding.songSearchInput.setOnItemClickListener { _, _, position, _ ->
            val selectedSongName = songAdapter.getItem(position)
            selectedSongName?.let { name ->
                val song = songsList.find { "${it.name} - ${it.artist}".equals(name, ignoreCase = true) }
                if (song != null) {
                    viewModel.selectSong(song)
                    showQuickLogDialog(song)
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
            val searchText = binding.songSearchInput.text?.toString()?.trim()
            if (searchText.isNullOrEmpty()) {
                showQuickSongDialog()
            } else {
                // Try to find or create song based on search
                handleQuickLog(searchText)
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
                viewModel.allSongs.collect { songs ->
                    songsList = songs
                    val songDisplayNames = songs.map { "${it.name} - ${it.artist}" }
                    songAdapter.clear()
                    songAdapter.addAll(songDisplayNames)
                    songAdapter.notifyDataSetChanged()
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
                "${it.name} ${it.artist}".contains(searchText, ignoreCase = true)
            }
            
            if (existingSong != null) {
                viewModel.selectSong(existingSong)
                showQuickLogDialog(existingSong)
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
                            binding.songSearchInput.text?.clear()
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
    
    private fun showQuickLogDialog(song: Song) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Performance")
            .setMessage("${song.name} - ${song.artist}")
            .setPositiveButton("Just Sang It!") { _, _ ->
                viewModel.logPerformance()
                binding.songSearchInput.text?.clear()
                Snackbar.make(binding.root, "Performance logged!", Snackbar.LENGTH_SHORT).show()
            }
            .setNeutralButton("With Notes...") { _, _ ->
                showDetailedLogDialog(song)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.clearSelectedSong()
            }
            .show()
    }
    
    private fun showDetailedLogDialog(song: Song) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_performance, null)
        // This layout would include key adjustment and notes inputs
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Performance Details")
            .setMessage("${song.name} - ${song.artist}")
            .setView(dialogView)
            .setPositiveButton("Log Performance") { _, _ ->
                // Get key adjustment and notes from dialog
                viewModel.logPerformance(keyAdjustment = 0, notes = null) // Simplified for now
                binding.songSearchInput.text?.clear()
                Snackbar.make(binding.root, "Performance logged with details!", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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