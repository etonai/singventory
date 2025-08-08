package com.pseddev.singventory.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentPurgeDataBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PurgeDataFragment : Fragment() {
    
    private var _binding: FragmentPurgeDataBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PurgeDataViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        PurgeDataViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            )
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurgeDataBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupButtons()
        
        // Load initial data overview
        viewModel.loadDataOverview()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataOverview.collect { overview ->
                    updateDataOverview(overview)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purgeState.collect { state ->
                    handlePurgeState(state)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    updateLoadingState(isLoading)
                }
            }
        }
    }
    
    private fun setupButtons() {
        binding.btnPurgeOldVisits.setOnClickListener {
            val totalVisits = viewModel.dataOverview.value.totalVisits
            val defaultCount = if (totalVisits >= 5) 5 else totalVisits
            showPurgeVisitsDialog(defaultCount)
        }
        
        binding.btnAnalyzeData.setOnClickListener {
            viewModel.analyzeDataForPurging()
        }
        
        binding.btnRecommendedPurge.setOnClickListener {
            viewModel.getRecommendedPurgeCount()?.let { count ->
                showPurgeVisitsDialog(count)
            }
        }
    }
    
    private fun updateDataOverview(overview: PurgeDataOverview) {
        with(binding) {
            totalVisitsText.text = "Total Visits: ${overview.totalVisits}"
            totalPerformancesText.text = "Total Performances: ${overview.totalPerformances}"
            oldestVisitText.text = "Oldest Visit: ${overview.oldestVisitDate}"
            newestVisitText.text = "Newest Visit: ${overview.newestVisitDate}"
            
            // Update the purge button text to show default count
            val defaultPurgeCount = if (overview.totalVisits >= 5) 5 else overview.totalVisits
            btnPurgeOldVisits.text = if (overview.totalVisits > 0) {
                "Purge $defaultPurgeCount Oldest Visits"
            } else {
                "No Visits to Purge"
            }
            btnPurgeOldVisits.isEnabled = overview.totalVisits > 0
            
            if (overview.totalVisits > 500) {
                storageWarningText.visibility = View.VISIBLE
                storageWarningText.text = "⚠️ You have ${overview.totalVisits} visits. Consider purging old data to improve performance."
                btnRecommendedPurge.visibility = View.VISIBLE
                recommendedPurgeText.text = "Recommended: Purge ${overview.recommendedPurgeCount} oldest visits"
            } else {
                storageWarningText.visibility = View.GONE
                btnRecommendedPurge.visibility = View.GONE
            }
            
            dataImpactText.text = "Purging removes detailed visit records while preserving song/venue statistics and performance counts."
        }
    }
    
    private fun showPurgeVisitsDialog(suggestedCount: Int = 5) {
        val message = if (suggestedCount > 5) {
            "Your app has many visits (${viewModel.dataOverview.value.totalVisits}). Consider purging $suggestedCount oldest visits to improve performance.\\n\\nThis will remove detailed visit records but preserve all song/venue statistics, performance counts, and last-performed dates."
        } else {
            "Purge the $suggestedCount oldest visits? This will remove detailed visit records but preserve all song/venue statistics and performance counts."
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Purge Old Visits")
            .setMessage(message)
            .setPositiveButton("Purge $suggestedCount Visits") { _, _ ->
                viewModel.purgeOldestVisits(suggestedCount)
            }
            .setNeutralButton("Custom Amount") { _, _ ->
                showCustomPurgeDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCustomPurgeDialog() {
        val editText = androidx.appcompat.widget.AppCompatEditText(requireContext())
        editText.hint = "Number of visits to purge"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Custom Purge Amount")
            .setMessage("How many of the oldest visits would you like to purge?")
            .setView(editText)
            .setPositiveButton("Purge") { _, _ ->
                val count = editText.text.toString().toIntOrNull() ?: 0
                if (count > 0) {
                    viewModel.purgeOldestVisits(count)
                } else {
                    Snackbar.make(binding.root, "Please enter a valid number", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun handlePurgeState(state: PurgeState) {
        when (state) {
            is PurgeState.Success -> {
                Snackbar.make(
                    binding.root, 
                    "Successfully purged ${state.deletedVisits} visits and ${state.deletedPerformances} performances", 
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.loadDataOverview() // Refresh data
                updatePurgeResult("Last purge: ${state.deletedVisits} visits, ${state.deletedPerformances} performances")
            }
            is PurgeState.Error -> {
                Snackbar.make(binding.root, "Purge failed: ${state.message}", Snackbar.LENGTH_LONG).show()
                updatePurgeResult("Last purge failed: ${state.message}")
            }
            is PurgeState.NoVisitsToPurge -> {
                Snackbar.make(binding.root, "No visits to purge", Snackbar.LENGTH_SHORT).show()
                updatePurgeResult("No visits available to purge")
            }
            PurgeState.Idle -> {
                updatePurgeResult("")
            }
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        val hasVisits = viewModel.dataOverview.value.totalVisits > 0
        binding.btnPurgeOldVisits.isEnabled = !isLoading && hasVisits
        binding.btnAnalyzeData.isEnabled = !isLoading
        binding.btnRecommendedPurge.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun updatePurgeResult(message: String) {
        binding.purgeResultText.text = message
        binding.purgeResultText.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetStates()
        _binding = null
    }
}