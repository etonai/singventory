package com.pseddev.singventory.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
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
    private var shouldScrollToTop = false
    
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
        setupSortControls()
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
                // Navigate to associate venues with song screen
                val bundle = Bundle().apply {
                    putLong("songId", song.id)
                }
                findNavController().navigate(R.id.action_songs_to_associateVenues, bundle)
            },
            onEditSongClick = { song ->
                // Navigate to edit song screen
                val bundle = Bundle().apply {
                    putLong("songId", song.id)
                }
                findNavController().navigate(R.id.action_songs_to_editSong, bundle)
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
        // Implement basic search functionality with text input
        binding.searchBar.setOnClickListener {
            // For now, create a simple search input
            val editText = androidx.appcompat.widget.AppCompatEditText(requireContext())
            editText.hint = "Search songs by name or artist"
            editText.setText(viewModel.searchQuery.value)
            
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Search Songs")
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
    
    private fun setupSortControls() {
        binding.btnSort.setOnClickListener { view ->
            showSortMenu(view)
        }
        
        binding.btnSortDirection.setOnClickListener {
            shouldScrollToTop = true
            viewModel.toggleSortOrder()
        }
    }
    
    private fun showSortMenu(anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.song_sort_menu, menu)
            
            // Set checked state for current sort option
            when (viewModel.sortOption.value) {
                SongSortOption.TITLE -> menu.findItem(R.id.sort_by_title)?.isChecked = true
                SongSortOption.ARTIST -> menu.findItem(R.id.sort_by_artist)?.isChecked = true
                SongSortOption.PERFORMANCE_COUNT -> menu.findItem(R.id.sort_by_performance_count)?.isChecked = true
                SongSortOption.LAST_PERFORMANCE -> menu.findItem(R.id.sort_by_last_performance)?.isChecked = true
            }
            
            setOnMenuItemClickListener { item ->
                val handled = when (item.itemId) {
                    R.id.sort_by_title -> {
                        viewModel.updateSortOption(SongSortOption.TITLE)
                        true
                    }
                    R.id.sort_by_artist -> {
                        viewModel.updateSortOption(SongSortOption.ARTIST)
                        true
                    }
                    R.id.sort_by_performance_count -> {
                        viewModel.updateSortOption(SongSortOption.PERFORMANCE_COUNT)
                        true
                    }
                    R.id.sort_by_last_performance -> {
                        viewModel.updateSortOption(SongSortOption.LAST_PERFORMANCE)
                        true
                    }
                    else -> false
                }
                
                // Set flag to scroll to top when data updates
                if (handled) {
                    shouldScrollToTop = true
                }
                
                handled
            }
            
            show()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songs.collect { songs ->
                    songsAdapter.submitList(songs) { 
                        // Scroll to top after the list has been updated if requested
                        if (shouldScrollToTop) {
                            binding.songsRecyclerView.scrollToPosition(0)
                            shouldScrollToTop = false
                        }
                    }
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
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sortAscending.collect { ascending ->
                    // Update sort direction icon
                    val iconRes = if (ascending) {
                        R.drawable.ic_sort_ascending
                    } else {
                        R.drawable.ic_sort_descending
                    }
                    binding.btnSortDirection.setIconResource(iconRes)
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
        
        // Save RecyclerView scroll position only if binding is available
        _binding?.let { binding ->
            val layoutManager = binding.songsRecyclerView.layoutManager as? LinearLayoutManager
            val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
            outState.putInt(KEY_SCROLL_POSITION, position)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}