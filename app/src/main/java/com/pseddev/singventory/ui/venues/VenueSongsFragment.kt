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
            onDeleteClick = { songVenueInfo ->
                showDeleteConfirmation(songVenueInfo)
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
    
    private fun showDeleteConfirmation(songVenueInfo: SongVenueInfoWithDetails) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Song Association")
            .setMessage("Remove \"${songVenueInfo.songName}\" from this venue's catalog? This will not delete the song itself, only its association with this venue.")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteSongVenueInfo(songVenueInfo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}