package com.pseddev.singventory.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentImportExportBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ImportExportFragment : Fragment() {
    
    private var _binding: FragmentImportExportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ImportExportViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        ImportExportViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            requireContext()
        )
    }
    
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.exportData(uri)
                }
            }
        }
        
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    showImportConfirmation(uri)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportExportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupButtons()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exportState.collect { state ->
                    handleExportState(state)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importState.collect { state ->
                    handleImportState(state)
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
        binding.btnExportData.setOnClickListener {
            showExportDialog()
        }
        
        binding.btnImportData.setOnClickListener {
            showImportDialog()
        }
    }
    
    private fun showExportDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export Data")
            .setMessage("Export all your songs, venues, visits, and performances to a JSON file for backup or transfer to another device.")
            .setPositiveButton("Export") { _, _ ->
                startExport()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "singventory_backup_${System.currentTimeMillis()}.json")
        }
        exportLauncher.launch(intent)
    }
    
    private fun showImportDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Import Data")
            .setMessage("Select a Singventory backup file to import. This will merge the imported data with your existing data.\\n\\nWARNING: This cannot be undone. Consider exporting your current data first.")
            .setPositiveButton("Select File") { _, _ ->
                startImport()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }
    
    private fun showImportConfirmation(uri: Uri) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Import")
            .setMessage("Are you sure you want to import data from this file? This will add the imported songs, venues, and visits to your existing data.\\n\\nThis action cannot be undone.")
            .setPositiveButton("Import") { _, _ ->
                viewModel.importData(uri)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun handleExportState(state: ImportExportState) {
        when (state) {
            is ImportExportState.Success -> {
                Snackbar.make(binding.root, "Export completed successfully", Snackbar.LENGTH_LONG).show()
                updateExportResult("Export completed successfully")
            }
            is ImportExportState.Error -> {
                Snackbar.make(binding.root, "Export failed: ${state.message}", Snackbar.LENGTH_LONG).show()
                updateExportResult("Export failed: ${state.message}")
            }
            is ImportExportState.Progress -> {
                updateExportResult("Exporting... ${state.message}")
            }
            ImportExportState.Idle -> {
                updateExportResult("")
            }
        }
    }
    
    private fun handleImportState(state: ImportExportState) {
        when (state) {
            is ImportExportState.Success -> {
                Snackbar.make(binding.root, "Import completed successfully", Snackbar.LENGTH_LONG).show()
                updateImportResult("Import completed successfully")
            }
            is ImportExportState.Error -> {
                Snackbar.make(binding.root, "Import failed: ${state.message}", Snackbar.LENGTH_LONG).show()
                updateImportResult("Import failed: ${state.message}")
            }
            is ImportExportState.Progress -> {
                updateImportResult("Importing... ${state.message}")
            }
            ImportExportState.Idle -> {
                updateImportResult("")
            }
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.btnExportData.isEnabled = !isLoading
        binding.btnImportData.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun updateExportResult(message: String) {
        binding.exportResult.text = message
        binding.exportResult.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun updateImportResult(message: String) {
        binding.importResult.text = message
        binding.importResult.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetStates()
        _binding = null
    }
}