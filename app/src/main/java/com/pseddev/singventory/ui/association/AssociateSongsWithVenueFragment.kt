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
import com.pseddev.singventory.databinding.FragmentAssociateSongsWithVenueBinding
import kotlinx.coroutines.launch

class AssociateSongsWithVenueFragment : Fragment() {

    private var _binding: FragmentAssociateSongsWithVenueBinding? = null
    private val binding get() = _binding!!

    private val venueId: Long by lazy {
        arguments?.getLong("venueId") ?: throw IllegalArgumentException("venueId is required")
    }

    private lateinit var songAssociationAdapter: SongAssociationAdapter
    private val viewModel: AssociateSongsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        AssociateSongsViewModel.Factory(
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
        _binding = FragmentAssociateSongsWithVenueBinding.inflate(inflater, container, false)
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
        songAssociationAdapter = SongAssociationAdapter(
            onSongSelect = { item, isSelected ->
                viewModel.toggleSongSelection(item.song.id, isSelected)
            },
            onSingleAssociate = { item ->
                if (item.existingAssociation != null) {
                    // Show association details dialog for update
                    showAssociationDetailsDialog(item)
                } else {
                    // Quick associate without details
                    lifecycleScope.launch {
                        val success = viewModel.associateSingleSong(item.song.id)
                        if (success) {
                            Snackbar.make(
                                binding.root,
                                "${item.song.name} associated with venue",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "Failed to associate song",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )

        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAssociationAdapter
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

        binding.chipFrequentlyPerformed.setOnCheckedChangeListener { _, isChecked ->
            updateFilter()
        }
    }

    private fun updateFilter() {
        val filter = AssociationFilter(
            showUnassociated = binding.chipUnassociated.isChecked,
            showAssociated = binding.chipAssociated.isChecked,
            showFrequentlyPerformed = binding.chipFrequentlyPerformed.isChecked
        )
        viewModel.updateFilter(filter)
    }

    private fun setupFab() {
        binding.fabBatchAssociate.setOnClickListener {
            val selectedItems = songAssociationAdapter.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                lifecycleScope.launch {
                    val songIds = selectedItems.map { it.song.id }
                    val success = viewModel.associateSongs(songIds)
                    if (success) {
                        val count = selectedItems.size
                        Snackbar.make(
                            binding.root,
                            "$count song${if (count == 1) "" else "s"} associated with venue",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        songAssociationAdapter.clearSelections()
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Failed to associate songs",
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
                viewModel.venue.collect { venue ->
                    venue?.let {
                        binding.venueName.text = it.name
                        binding.venueAddress.text = it.address ?: "Address not specified"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.associatedSongsCount.collect { count ->
                    binding.associatedSongsCount.text = when (count) {
                        0 -> "No songs associated yet"
                        1 -> "1 song associated"
                        else -> "$count songs associated"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songAssociationItems.collect { items ->
                    songAssociationAdapter.submitList(items)
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

    private fun showAssociationDetailsDialog(item: SongAssociationItem) {
        // TODO: Implement association details dialog
        // For now, just show a simple association
        lifecycleScope.launch {
            val success = viewModel.associateSingleSong(item.song.id)
            if (success) {
                Snackbar.make(
                    binding.root,
                    "${item.song.name} association updated",
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