package com.pseddev.singventory.ui.visits

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
import com.pseddev.singventory.databinding.FragmentVisitsBinding
import kotlinx.coroutines.launch

class VisitsFragment : Fragment() {
    
    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var visitsAdapter: VisitsAdapter
    private val viewModel: VisitsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        VisitsViewModel.Factory(
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
        setupObservers()
        restoreState(savedInstanceState)
    }
    
    private fun setupRecyclerView() {
        visitsAdapter = VisitsAdapter(
            onVisitClick = { visitWithDetails ->
                // Navigate to visit details/ActiveVisitFragment
                if (visitWithDetails.visit.endTimestamp == null) {
                    // Active visit - navigate to ActiveVisitFragment
                    findNavController().navigate(
                        R.id.action_visits_to_activeVisit,
                        bundleOf("visitId" to visitWithDetails.visit.id)
                    )
                } else {
                    // Completed visit - navigate to visit summary/details
                    // Navigation to be implemented - would go to a CompletedVisitFragment
                }
            },
            onResumeVisitClick = { visitWithDetails ->
                // Navigate to ActiveVisitFragment for this visit
                viewModel.setActiveVisit(visitWithDetails)
                findNavController().navigate(
                    R.id.action_visits_to_activeVisit,
                    bundleOf("visitId" to visitWithDetails.visit.id)
                )
            },
            onEndVisitClick = { visitWithDetails ->
                // Show confirmation dialog and end visit
                showEndVisitConfirmation(visitWithDetails)
            }
        )
        
        binding.visitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = visitsAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabStartVisit.setOnClickListener {
            findNavController().navigate(R.id.action_visits_to_startVisit)
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visits.collect { visits ->
                    visitsAdapter.submitList(visits)
                    updateEmptyState(visits.isEmpty())
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeVisit.collect { activeVisit ->
                    activeVisitId = activeVisit?.id
                    // Update UI to highlight active visit if needed
                }
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.visitsRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.visitsRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
    
    private fun showEndVisitConfirmation(visitWithDetails: VisitWithDetails) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("End Visit")
            .setMessage("Are you sure you want to end your visit to ${visitWithDetails.venueName}?")
            .setPositiveButton("End Visit") { _, _ ->
                viewModel.endVisit(visitWithDetails)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            scrollPosition = bundle.getInt(KEY_SCROLL_POSITION, 0)
            activeVisitId = bundle.getLong(KEY_ACTIVE_VISIT_ID, -1L).takeIf { it != -1L }
            
            // Restore scroll position when data is loaded
            if (scrollPosition > 0) {
                binding.visitsRecyclerView.scrollToPosition(scrollPosition)
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save RecyclerView scroll position only if binding is available
        _binding?.let { binding ->
            val layoutManager = binding.visitsRecyclerView.layoutManager as? LinearLayoutManager
            val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
            outState.putInt(KEY_SCROLL_POSITION, position)
        }
        
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