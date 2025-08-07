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

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupFab()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        venueAssociationAdapter = VenueAssociationAdapter(
            onVenueSelect = { item, isSelected ->
                viewModel.toggleVenueSelection(item.venue.id, isSelected)
            },
            onSingleAssociate = { item ->
                if (item.existingAssociation != null) {
                    // Show association details dialog for update
                    showAssociationDetailsDialog(item)
                } else {
                    // Quick associate without details
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
        // For now, implement a simple click listener that shows a basic search
        // In a full implementation, you'd use SearchView with expansion
        binding.searchBar.setOnClickListener {
            // TODO: Implement proper search functionality
            // For now, just enable simple text-based filtering
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
        // TODO: Implement association details dialog
        // For now, just show a simple association
        lifecycleScope.launch {
            val success = viewModel.associateSingleVenue(item.venue.id)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}