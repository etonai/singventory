package com.pseddev.singventory.ui.visits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentStartVisitBinding
import kotlinx.coroutines.launch

class StartVisitFragment : Fragment() {
    
    private var _binding: FragmentStartVisitBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: StartVisitViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        StartVisitViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            )
        )
    }
    
    private lateinit var venueAdapter: ArrayAdapter<String>
    private var venuesList: List<Venue> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartVisitBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupVenueDropdown()
        setupFormInputs()
        setupButtons()
        setupObservers()
    }
    
    private fun setupVenueDropdown() {
        venueAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.venueSelectionInput.setAdapter(venueAdapter)
        
        binding.venueSelectionInput.setOnItemClickListener { _, _, position, _ ->
            val selectedVenueName = venueAdapter.getItem(position)
            selectedVenueName?.let { name ->
                val venue = venuesList.find { it.name == name }
                if (venue != null) {
                    viewModel.selectVenue(venue)
                    clearError(binding.venueSelectionLayout)
                }
            }
        }
    }
    
    private fun setupFormInputs() {
        binding.visitNotesInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setVisitNotes(text?.toString()?.takeIf { it.isNotBlank() })
        }
    }
    
    private fun setupButtons() {
        binding.btnCreateNewVenue.setOnClickListener {
            showCreateNewVenueDialog()
        }
        
        binding.fabStartVisit.setOnClickListener {
            if (validateForm()) {
                viewModel.startVisit()
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allVenues.collect { venues ->
                    venuesList = venues
                    val venueNames = venues.map { it.name }
                    venueAdapter.clear()
                    venueAdapter.addAll(venueNames)
                    venueAdapter.notifyDataSetChanged()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedVenue.collect { venue ->
                    if (venue != null) {
                        binding.venueSelectionInput.setText(venue.name, false)
                        clearError(binding.venueSelectionLayout)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCreatingVisit.collect { isCreating ->
                    binding.fabStartVisit.isEnabled = !isCreating
                    if (isCreating) {
                        binding.fabStartVisit.text = "Starting..."
                    } else {
                        binding.fabStartVisit.text = "Start Visit"
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visitCreated.collect { visit ->
                    visit?.let {
                        Snackbar.make(binding.root, "Visit started at ${viewModel.selectedVenue.value?.name}!", Snackbar.LENGTH_LONG).show()
                        
                        // Navigate to ActiveVisitFragment
                        // For now, navigate back to visits list
                        findNavController().navigateUp()
                        viewModel.clearVisitCreated()
                    }
                }
            }
        }
    }
    
    private fun showCreateNewVenueDialog() {
        val currentQuery = binding.venueSelectionInput.text?.toString()?.trim() ?: ""
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_venue, null)
        val venueNameInput = dialogView.findViewById<TextInputEditText>(R.id.venue_name_input)
        val venueNameLayout = dialogView.findViewById<TextInputLayout>(R.id.venue_name_layout)
        
        // Pre-fill with current search query if it's not empty
        if (currentQuery.isNotEmpty()) {
            venueNameInput.setText(currentQuery)
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Venue")
            .setMessage("Enter the venue name. You can add more details later.")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val venueName = venueNameInput.text?.toString()?.trim()
                if (!venueName.isNullOrBlank()) {
                    viewModel.createQuickVenue(venueName)
                } else {
                    venueNameLayout.error = "Venue name is required"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun validateForm(): Boolean {
        val selectedVenue = viewModel.selectedVenue.value
        if (selectedVenue == null) {
            binding.venueSelectionLayout.error = "Please select or create a venue"
            return false
        }
        
        return true
    }
    
    private fun clearError(layout: TextInputLayout) {
        layout.error = null
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}