package com.pseddev.singventory.ui.visits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.singventory.databinding.FragmentVisitsBinding

class VisitsFragment : Fragment() {
    
    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val KEY_SCROLL_POSITION = "scroll_position"
        private const val KEY_ACTIVE_VISIT_ID = "active_visit_id"
    }
    
    private var scrollPosition: Int = 0
    private var activeVisitId: Long? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        restoreState(savedInstanceState)
        
        // Show empty view initially (will be replaced with actual data in Phase 3)
        showEmptyState()
    }
    
    private fun setupRecyclerView() {
        binding.visitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // Adapter will be added in Phase 3 when we implement visit management
        }
    }
    
    private fun setupFab() {
        binding.fabStartVisit.setOnClickListener {
            // Navigate to StartVisitFragment in Phase 3
            // For now, just show a placeholder
        }
    }
    
    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            scrollPosition = bundle.getInt(KEY_SCROLL_POSITION, 0)
            activeVisitId = bundle.getLong(KEY_ACTIVE_VISIT_ID, -1L).takeIf { it != -1L }
            
            // Scroll position and active visit state will be restored when data is loaded in Phase 3
        }
    }
    
    private fun showEmptyState() {
        binding.visitsRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save RecyclerView scroll position
        val layoutManager = binding.visitsRecyclerView.layoutManager as? LinearLayoutManager
        val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
        outState.putInt(KEY_SCROLL_POSITION, position)
        
        // Save active visit ID if any
        activeVisitId?.let { visitId ->
            outState.putLong(KEY_ACTIVE_VISIT_ID, visitId)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}