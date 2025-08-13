package com.pseddev.singventory.ui.venues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentVenueSongsBinding
import kotlinx.coroutines.launch

class VenueSongsFragment : Fragment() {
    
    private var _binding: FragmentVenueSongsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var venueSongsAdapter: VenueSongsAdapter
    private val viewModel: VenueSongsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        val venueId = arguments?.getLong("venueId") ?: -1L
        VenueSongsViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            venueId
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVenueSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSearch()
    }
    
    private fun setupRecyclerView() {
        venueSongsAdapter = VenueSongsAdapter(
            onSongClick = { songVenueInfo ->
                // Navigate to edit song-venue association
                findNavController().navigate(
                    R.id.action_venueSongs_to_editSongVenueInfo,
                    bundleOf("songVenueInfoId" to songVenueInfo.id)
                )
            },
            onAddPerformance = { songVenueInfo ->
                // Navigate to Add Performance screen with pre-selected song
                lifecycleScope.launch {
                    val activeVisit = viewModel.getActiveVisitForVenue()
                    activeVisit?.let { visit ->
                        findNavController().navigate(
                            R.id.action_venueSongs_to_addPerformance,
                            bundleOf(
                                "visitId" to visit.id,
                                "preSelectedSongId" to songVenueInfo.songVenueInfo.songId
                            )
                        )
                    }
                }
            }
        )
        
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = venueSongsAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.venueInfo.collect { venueInfo ->
                    venueInfo?.let { venue ->
                        binding.venueName.text = venue.name
                        binding.venueAddress.text = venue.address ?: "No address specified"
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredSongs.collect { songs ->
                    venueSongsAdapter.submitList(songs)
                    updateEmptyState(songs.isEmpty())
                    updateSongCount(songs.size)
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
                viewModel.hasActiveVisit.collect { hasActiveVisit ->
                    binding.activeVisitTag.visibility = if (hasActiveVisit) View.VISIBLE else View.GONE
                    venueSongsAdapter.updateActiveVisitStatus(hasActiveVisit)
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddSong.setOnClickListener {
            // Navigate to associate songs with venue fragment
            val venueId = arguments?.getLong("venueId") ?: -1L
            findNavController().navigate(
                R.id.action_venueSongs_to_associateSongs,
                bundleOf("venueId" to venueId)
            )
        }
        
        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.setQuery("", false)
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText ?: "")
                binding.btnClearSearch.visibility = 
                    if (newText.isNullOrEmpty()) View.GONE else View.VISIBLE
                return true
            }
        })
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.songsRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.songsRecyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }
    
    private fun updateSongCount(count: Int) {
        binding.songCount.text = if (count == 1) "1 song" else "$count songs"
    }
    
    private fun showSongInfoDialog(songVenueInfoWithDetails: SongVenueInfoWithDetails) {
        lifecycleScope.launch {
            val songVenueInfo = songVenueInfoWithDetails.songVenueInfo
            val venueInfo = viewModel.getSongVenueInfo(songVenueInfo.songId)
            
            // Create comprehensive song information display
            val songInfo = buildString {
                append("Song: ${songVenueInfoWithDetails.songName}\n")
                if (songVenueInfoWithDetails.artistName.isNotBlank()) {
                    append("Artist: ${songVenueInfoWithDetails.artistName}\n")
                }
                
                // Display venue song number if available
                if (!songVenueInfo.venuesSongId.isNullOrBlank()) {
                    append("Venue Song #: ${songVenueInfo.venuesSongId}\n")
                }
                append("\n")
                
                // Key information
                append("Key Information:\n")
                if (!songVenueInfo.venueKey.isNullOrBlank()) {
                    append("Venue Key: ${songVenueInfo.venueKey}\n")
                }
                val keyAdjustment = songVenueInfo.keyAdjustment
                if (keyAdjustment != 0 && !com.pseddev.singventory.data.entity.SongVenueInfo.isKeyAdjustmentUnknown(keyAdjustment)) {
                    val adjustmentString = if (keyAdjustment > 0) "+$keyAdjustment" else keyAdjustment.toString()
                    append("Key Adjustment: $adjustmentString steps\n")
                }
                
                // Performance history
                append("\nPerformance History:\n")
                val performanceText = when (songVenueInfoWithDetails.performanceCount) {
                    0 -> "Never performed at this venue"
                    1 -> "Performed 1 time at this venue"
                    else -> "Performed ${songVenueInfoWithDetails.performanceCount} times at this venue"
                }
                append(performanceText)
            }
            
            // Show dialog on main thread
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Song Info")
                .setMessage(songInfo)
                .setPositiveButton("Just Sang It!") { _, _ ->
                    lifecycleScope.launch {
                        val success = viewModel.logPerformance(songVenueInfo.songId)
                        if (success) {
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root, 
                                "Performance logged!", 
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root, 
                                "Error logging performance", 
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNeutralButton("With Notes...") { _, _ ->
                    showDetailedSongInfoDialog(songVenueInfoWithDetails)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun showDetailedSongInfoDialog(songVenueInfoWithDetails: SongVenueInfoWithDetails) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_performance, null)
        val notesInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.performance_notes_input)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Performance with Notes")
            .setView(dialogView)
            .setPositiveButton("Log Performance") { _, _ ->
                val notes = notesInput?.text?.toString()?.trim()
                lifecycleScope.launch {
                    val success = viewModel.logPerformance(
                        songVenueInfoWithDetails.songVenueInfo.songId,
                        notes = if (notes.isNullOrBlank()) null else notes
                    )
                    if (success) {
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root, 
                            "Performance logged with notes!", 
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root, 
                            "Error logging performance", 
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}