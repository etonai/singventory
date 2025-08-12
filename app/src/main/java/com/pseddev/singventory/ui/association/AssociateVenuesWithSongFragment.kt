package com.pseddev.singventory.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentAssociateVenuesWithSongBinding
import kotlinx.coroutines.launch

class AssociateVenuesWithSongFragment : Fragment() {

    private var _binding: FragmentAssociateVenuesWithSongBinding? = null
    private val binding get() = _binding!!

    private val songId: Long by lazy {
        arguments?.getLong("songId") ?: throw IllegalArgumentException("songId is required")
    }

    private lateinit var venueAssociationAdapter: VenueAssociationAdapter
    private val viewModel: AssociateVenuesViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        AssociateVenuesViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            songId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssociateVenuesWithSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupFab()
        setupObservers()
    }

    private fun setupRecyclerView() {
        venueAssociationAdapter = VenueAssociationAdapter(
            onVenueSelect = { item, isSelected ->
                viewModel.toggleVenueSelection(item.venue.id, isSelected)
            },
            onSingleAssociate = { item ->
                val song = viewModel.song.value
                val hasKeyInfo = song?.referenceKey != null || song?.preferredKey != null
                
                if (item.existingAssociation != null || hasKeyInfo) {
                    // Show association details dialog for update OR if song has key info for smart auto-population
                    showAssociationDetailsDialog(item)
                } else {
                    // Quick associate without details (only if song has no key info)
                    lifecycleScope.launch {
                        val success = viewModel.associateSingleVenue(item.venue.id)
                        if (success) {
                            Snackbar.make(
                                binding.root,
                                "Song associated with ${item.venue.name}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "Failed to associate venue",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )

        binding.venuesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = venueAssociationAdapter
        }
    }

    private fun setupSearch() {
        // Implement basic search functionality with text input
        binding.searchBar.setOnClickListener {
            // Create a simple search input
            val editText = androidx.appcompat.widget.AppCompatEditText(requireContext())
            editText.hint = "Search venues by name or address"
            editText.setText(viewModel.searchQuery.value)
            
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Search Venues")
                .setView(editText)
                .setPositiveButton("Search") { _, _ ->
                    viewModel.updateSearchQuery(editText.text.toString())
                }
                .setNegativeButton("Clear") { _, _ ->
                    viewModel.updateSearchQuery("")
                }
                .setNeutralButton("Cancel", null)
                .show()
        }
    }

    private fun setupFilters() {
        binding.btnFilter.setOnClickListener {
            val isVisible = binding.filterChips.visibility == View.VISIBLE
            binding.filterChips.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        binding.chipUnassociated.setOnCheckedChangeListener { _, isChecked ->
            updateFilter()
        }

        binding.chipAssociated.setOnCheckedChangeListener { _, isChecked ->
            updateFilter()
        }

        binding.chipFrequentlyVisited.setOnCheckedChangeListener { _, isChecked ->
            updateFilter()
        }
    }

    private fun updateFilter() {
        val filter = VenueAssociationFilter(
            showUnassociated = binding.chipUnassociated.isChecked,
            showAssociated = binding.chipAssociated.isChecked,
            showFrequentlyVisited = binding.chipFrequentlyVisited.isChecked
        )
        viewModel.updateFilter(filter)
    }

    private fun setupFab() {
        binding.fabBatchAssociate.setOnClickListener {
            val selectedItems = venueAssociationAdapter.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                lifecycleScope.launch {
                    val venueIds = selectedItems.map { it.venue.id }
                    val success = viewModel.associateVenues(venueIds)
                    if (success) {
                        val count = selectedItems.size
                        Snackbar.make(
                            binding.root,
                            "Song associated with $count venue${if (count == 1) "" else "s"}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        venueAssociationAdapter.clearSelections()
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Failed to associate venues",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.song.collect { song ->
                    song?.let {
                        binding.songTitle.text = it.name
                        binding.songArtist.text = if (it.artist.isBlank()) "Artist not specified" else it.artist
                        
                        // Show key information if available
                        val keyInfo = when {
                            it.preferredKey != null && it.referenceKey != null -> {
                                "Preferred: ${it.preferredKey} (Reference: ${it.referenceKey})"
                            }
                            it.preferredKey != null -> "Key: ${it.preferredKey}"
                            it.referenceKey != null -> "Reference: ${it.referenceKey}"
                            else -> "No key information"
                        }
                        binding.songKeyInfo.text = keyInfo
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.associatedVenuesCount.collect { count ->
                    binding.associatedVenuesCount.text = when (count) {
                        0 -> "Not available at any venue yet"
                        1 -> "Available at 1 venue"
                        else -> "Available at $count venues"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.venueAssociationItems.collect { items ->
                    venueAssociationAdapter.submitList(items)
                    binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedItems.collect { selectedIds ->
                    val hasSelection = selectedIds.isNotEmpty()
                    binding.fabBatchAssociate.visibility = if (hasSelection) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showAssociationDetailsDialog(item: VenueAssociationItem) {
        val song = viewModel.song.value
        if (song == null) {
            Snackbar.make(binding.root, "Song information not available", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val dialog = AssociationDetailsDialog.newInstance(
            songName = song.name,
            venueName = item.venue.name,
            existingVenueSongId = item.existingAssociation?.venuesSongId,
            existingVenueKey = item.existingAssociation?.venueKey,
            existingKeyAdjustment = item.existingAssociation?.keyAdjustment ?: 0,
            songReferenceKey = song.referenceKey,
            songPreferredKey = song.preferredKey
        )
        
        dialog.onDetailsConfirmed = { details ->
            lifecycleScope.launch {
                val success = viewModel.associateVenueWithDetails(
                    venueId = item.venue.id,
                    venueSongId = details.venueSongId,
                    venueKey = details.venueKey,
                    keyAdjustment = details.keyAdjustment
                )
                if (success) {
                    Snackbar.make(
                        binding.root,
                        "Association with ${item.venue.name} updated",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Failed to update association",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        dialog.show(parentFragmentManager, "AssociationDetailsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}