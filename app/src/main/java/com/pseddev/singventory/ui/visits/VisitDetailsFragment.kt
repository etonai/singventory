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
import com.pseddev.singventory.databinding.FragmentVisitDetailsBinding
import kotlinx.coroutines.launch

class VisitDetailsFragment : Fragment() {
    
    private var _binding: FragmentVisitDetailsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var performancesAdapter: PerformancesAdapter
    private val viewModel: VisitDetailsViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        val visitId = arguments?.getLong("visitId") ?: -1L
        VisitDetailsViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            visitId
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        performancesAdapter = PerformancesAdapter { performanceWithSong ->
            // Navigate to performance edit fragment
            findNavController().navigate(
                R.id.action_visitDetails_to_performanceEdit,
                bundleOf("performanceId" to performanceWithSong.performance.id)
            )
        }
        
        binding.performancesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = performancesAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visitDetails.collect { visitDetails ->
                    visitDetails?.let { details ->
                        updateVisitInfo(details)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.performances.collect { performances ->
                    performancesAdapter.submitList(performances)
                    updatePerformanceCount(performances.size)
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
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteResult.collect { result ->
                    result?.let { success ->
                        if (success) {
                            findNavController().navigateUp()
                        } else {
                            // Show error message
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root, 
                                "Failed to delete visit", 
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        viewModel.clearDeleteResult()
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveVisitChanges()
        }
        
        binding.btnAddPerformance.setOnClickListener {
            // Navigate to AddPerformanceFragment for comprehensive performance management
            val visitId = arguments?.getLong("visitId") ?: -1L
            findNavController().navigate(
                R.id.action_visitDetails_to_addPerformance,
                bundleOf("visitId" to visitId)
            )
        }
        
        binding.btnDeleteVisit.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun updateVisitInfo(visitDetails: VisitDetailsData) {
        binding.venueName.text = visitDetails.venueName
        binding.visitDateTime.text = visitDetails.formattedDateTime
        binding.visitStatus.text = if (visitDetails.visit.endTimestamp != null) "Completed" else "Active"
        
        // Populate editable fields
        binding.etNotes.setText(visitDetails.visit.notes ?: "")
        binding.etAmountSpent.setText(visitDetails.visit.amountSpent?.toString() ?: "")
        
        // Hide duration display - removed as not useful to users
        
        // Show Add Performance button for all visits (both active and completed)
        binding.btnAddPerformance.visibility = View.VISIBLE
    }
    
    private fun updatePerformanceCount(count: Int) {
        binding.performanceCount.text = if (count == 1) "1 performance" else "$count performances"
    }
    
    private fun saveVisitChanges() {
        val notes = binding.etNotes.text.toString().trim().takeIf { it.isNotBlank() }
        val amountSpentText = binding.etAmountSpent.text.toString().trim()
        val amountSpent = amountSpentText.toDoubleOrNull()
        
        viewModel.saveVisitChanges(notes, amountSpent)
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Visit")
            .setMessage("Are you sure you want to delete this visit and all its performances? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteVisit()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}