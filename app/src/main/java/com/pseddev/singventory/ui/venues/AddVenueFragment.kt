package com.pseddev.singventory.ui.venues

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
import com.google.android.material.snackbar.Snackbar
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentAddVenueBinding
import kotlinx.coroutines.launch

class AddVenueFragment : Fragment() {
    
    private var _binding: FragmentAddVenueBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddVenueViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        AddVenueViewModel.Factory(
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
        _binding = FragmentAddVenueBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRoomTypeDropdown()
        setupFormValidation()
        setupSaveButton()
        setupObservers()
    }
    
    private fun setupRoomTypeDropdown() {
        val roomTypeOptions = listOf("", "Private", "Public", "Both")
        val roomTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypeOptions)
        
        binding.roomTypeInput.setAdapter(roomTypeAdapter)
        
        binding.roomTypeInput.setOnItemClickListener { _, _, position, _ ->
            val selectedRoomType = if (position == 0) null else roomTypeOptions[position]
            viewModel.setRoomType(selectedRoomType)
        }
    }
    
    private fun setupFormValidation() {
        binding.venueNameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setVenueName(text?.toString() ?: "")
            clearError(binding.venueNameLayout)
        }
        
        binding.venueAddressInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setAddress(text?.toString()?.takeIf { it.isNotBlank() })
        }
        
        binding.costInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setCost(text?.toString()?.takeIf { it.isNotBlank() })
        }
        
        binding.hoursInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setHours(text?.toString()?.takeIf { it.isNotBlank() })
        }
        
        binding.notesInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setNotes(text?.toString()?.takeIf { it.isNotBlank() })
        }
    }
    
    private fun setupSaveButton() {
        binding.fabSaveVenue.setOnClickListener {
            if (validateForm()) {
                viewModel.saveVenue()
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSaving.collect { isSaving ->
                    binding.fabSaveVenue.isEnabled = !isSaving
                    if (isSaving) {
                        binding.fabSaveVenue.text = "Saving..."
                    } else {
                        binding.fabSaveVenue.text = "Save Venue"
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result?.let { success ->
                        if (success) {
                            Snackbar.make(binding.root, "Venue saved successfully!", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Snackbar.make(binding.root, "Failed to save venue. Please try again.", Snackbar.LENGTH_LONG).show()
                        }
                        viewModel.clearSaveResult()
                    }
                }
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        val venueName = binding.venueNameInput.text?.toString()?.trim()
        if (venueName.isNullOrBlank()) {
            binding.venueNameLayout.error = "Venue name is required"
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearError(layout: com.google.android.material.textfield.TextInputLayout) {
        layout.error = null
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}