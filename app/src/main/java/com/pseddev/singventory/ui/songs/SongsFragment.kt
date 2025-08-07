package com.pseddev.singventory.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
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
import com.pseddev.singventory.databinding.FragmentSongsBinding
import kotlinx.coroutines.launch

class SongsFragment : Fragment() {
    
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var songsAdapter: SongsAdapter
    private val viewModel: SongsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        SongsViewModel.Factory(
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
        setupObservers()
        restoreState(savedInstanceState)
    }
    
    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter(
            onSongClick = { song ->
                // Navigate to song details/edit screen (to be implemented)
                // For now, just log
            },
            onAddVenueClick = { song ->
                // Navigate to add venue to song screen (to be implemented)
                // For now, just log
            }
        )
        
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddSong.setOnClickListener {
            findNavController().navigate(R.id.action_songs_to_addSong)
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
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songs.collect { songs ->
                    songsAdapter.submitList(songs)
                    updateEmptyState(songs.isEmpty())
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
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.songsRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.songsRecyclerView.visibility = View.VISIBLE
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
                binding.songsRecyclerView.scrollToPosition(scrollPosition)
            }
        }
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