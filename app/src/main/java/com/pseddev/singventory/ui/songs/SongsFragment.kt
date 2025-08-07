package com.pseddev.singventory.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.singventory.databinding.FragmentSongsBinding

class SongsFragment : Fragment() {
    
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_SCROLL_POSITION = "scroll_position"
    }
    
    private var searchQuery: String = ""
    private var scrollPosition: Int = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        setupSearchBar()
        restoreState(savedInstanceState)
        
        // Show empty view initially (will be replaced with actual data in Phase 2)
        showEmptyState()
    }
    
    private fun setupRecyclerView() {
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // Adapter will be added in Phase 2 when we implement CRUD operations
        }
    }
    
    private fun setupFab() {
        binding.fabAddSong.setOnClickListener {
            // Navigate to AddSongFragment in Phase 2
            // For now, just show a placeholder
        }
    }
    
    private fun setupSearchBar() {
        // Search functionality will be implemented in Phase 2
        // This sets up the basic structure for state preservation
        binding.searchBar.setOnClickListener {
            // SearchBar click handler - will implement search expansion in Phase 2
            // For now, just update the search query tracking
        }
        
        // SearchBar text changes will be handled when we implement search in Phase 2
    }
    
    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            searchQuery = bundle.getString(KEY_SEARCH_QUERY, "")
            scrollPosition = bundle.getInt(KEY_SCROLL_POSITION, 0)
            
            // Restore search query
            if (searchQuery.isNotEmpty()) {
                binding.searchBar.setText(searchQuery)
            }
            
            // Scroll position will be restored when data is loaded in Phase 2
        }
    }
    
    private fun showEmptyState() {
        binding.songsRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        outState.putString(KEY_SEARCH_QUERY, searchQuery)
        
        // Save RecyclerView scroll position
        val layoutManager = binding.songsRecyclerView.layoutManager as? LinearLayoutManager
        val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
        outState.putInt(KEY_SCROLL_POSITION, position)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}