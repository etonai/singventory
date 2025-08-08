package com.pseddev.singventory.ui.visits

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentPerformanceEditBinding
import kotlinx.coroutines.launch

class PerformanceEditFragment : Fragment() {
    
    private var _binding: FragmentPerformanceEditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerformanceEditViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        val performanceId = arguments?.getLong("performanceId") ?: -1L
        PerformanceEditViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            performanceId
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.performanceDetails.collect { details ->
                    details?.let { performanceDetails ->
                        updateUI(performanceDetails)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.btnSaveChanges.isEnabled = !isLoading
                    binding.btnDeletePerformance.isEnabled = !isLoading
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
        
        binding.btnDeletePerformance.setOnClickListener {
            showDeleteConfirmation()
        }
        
        // Key adjustment buttons
        binding.btnKeyMinus.setOnClickListener {
            adjustKeyValue(-1)
        }
        
        binding.btnKeyPlus.setOnClickListener {
            adjustKeyValue(1)
        }
    }
    
    private fun updateUI(details: PerformanceEditData) {
        binding.songTitle.text = details.songName
        binding.artistName.text = details.artistName
        binding.performanceTime.text = details.formattedTime
        
        // Set key adjustment
        updateKeyAdjustmentDisplay(details.performance.keyAdjustment)
        
        // Set notes
        binding.etNotes.setText(details.performance.notes ?: "")
    }
    
    private fun updateKeyAdjustmentDisplay(keyAdjustment: Int) {
        binding.keyAdjustmentValue.text = when {
            keyAdjustment > 0 -> "+$keyAdjustment"
            keyAdjustment < 0 -> keyAdjustment.toString()
            else -> "0"
        }
    }
    
    private fun adjustKeyValue(adjustment: Int) {
        val currentText = binding.keyAdjustmentValue.text.toString()
        val currentValue = when (currentText) {
            "0" -> 0
            else -> currentText.toIntOrNull() ?: 0
        }
        val newValue = (currentValue + adjustment).coerceIn(-12, 12)
        updateKeyAdjustmentDisplay(newValue)
    }
    
    private fun saveChanges() {
        val keyAdjustmentText = binding.keyAdjustmentValue.text.toString()
        val keyAdjustment = when (keyAdjustmentText) {
            "0" -> 0
            else -> keyAdjustmentText.toIntOrNull() ?: 0
        }
        
        val notes = binding.etNotes.text.toString().trim().takeIf { it.isNotBlank() }
        
        viewModel.saveChanges(keyAdjustment, notes)
        
        // Navigate back
        findNavController().navigateUp()
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Performance")
            .setMessage("Are you sure you want to delete this performance? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePerformance()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}