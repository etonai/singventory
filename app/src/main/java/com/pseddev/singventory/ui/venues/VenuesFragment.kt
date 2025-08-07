package com.pseddev.singventory.ui.venues

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
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentVenuesBinding
import kotlinx.coroutines.launch

class VenuesFragment : Fragment() {
    
    private var _binding: FragmentVenuesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var venuesAdapter: VenuesAdapter
    private val viewModel: VenuesViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        VenuesViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            )
        )
    }
    
    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_SCROLL_POSITION = "scroll_position"
    }
    
    private var searchQuery: String = ""
    private var scrollPosition: Int = 0
    private var shouldScrollToTop = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVenuesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        setupSearchBar()
        setupSortControls()
        setupObservers()
        restoreState(savedInstanceState)
    }
    
    private fun setupRecyclerView() {
        venuesAdapter = VenuesAdapter(
            onVenueClick = { venue ->
                // Navigate to venue details/edit screen (to be implemented)
                // For now, just log
            },
            onAddSongToVenueClick = { venue ->
                // Navigate to add song to venue screen (to be implemented)
                // For now, just log
            }
        )
        
        binding.venuesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = venuesAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddVenue.setOnClickListener {
            findNavController().navigate(R.id.action_venues_to_addVenue)
        }
    }
    
    private fun setupSearchBar() {
        // For now, use a simple text change listener on the SearchBar
        // In a full implementation, you'd use SearchView with proper expansion
        binding.searchBar.setOnClickListener {
            // Simple implementation - in real app would expand to SearchView
            // For now, just focus for typing
        }
    }
    
    private fun setupSortControls() {
        binding.btnSortToggle.setOnClickListener {
            shouldScrollToTop = true
            viewModel.toggleSortOrder()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.venues.collect { venues ->
                    venuesAdapter.submitList(venues) { 
                        // Scroll to top after the list has been updated if requested
                        if (shouldScrollToTop) {
                            binding.venuesRecyclerView.scrollToPosition(0)
                            shouldScrollToTop = false
                        }
                    }
                    updateEmptyState(venues.isEmpty())
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchQuery.collect { query ->
                    searchQuery = query
                    // Update search bar text if needed
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sortAscending.collect { ascending ->
                    // Update sort toggle icon
                    val iconRes = if (ascending) {
                        R.drawable.ic_sort_ascending
                    } else {
                        R.drawable.ic_sort_descending
                    }
                    binding.btnSortToggle.setIconResource(iconRes)
                }
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.venuesRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.venuesRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
    
    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            searchQuery = bundle.getString(KEY_SEARCH_QUERY, "")
            scrollPosition = bundle.getInt(KEY_SCROLL_POSITION, 0)
            
            // Restore search query  
            viewModel.updateSearchQuery(searchQuery)
            
            // Scroll position will be restored when data is loaded
            if (scrollPosition > 0) {
                binding.venuesRecyclerView.scrollToPosition(scrollPosition)
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        outState.putString(KEY_SEARCH_QUERY, searchQuery)
        
        // Save RecyclerView scroll position only if binding is available
        _binding?.let { binding ->
            val layoutManager = binding.venuesRecyclerView.layoutManager as? LinearLayoutManager
            val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
            outState.putInt(KEY_SCROLL_POSITION, position)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}