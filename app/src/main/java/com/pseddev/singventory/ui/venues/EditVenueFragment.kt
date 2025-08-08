package com.pseddev.singventory.ui.venues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pseddev.singventory.R
import com.pseddev.singventory.data.database.SingventoryDatabase
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentEditVenueBinding
import kotlinx.coroutines.launch

class EditVenueFragment : Fragment() {

    private var _binding: FragmentEditVenueBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditVenueViewModel by viewModels {
        val venueId = arguments?.getLong("venueId") ?: 0L
        val database = SingventoryDatabase.getDatabase(requireContext())
        EditVenueViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            venueId
        )
    }

    private lateinit var roomTypeAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditVenueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set initial title while data loads
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Venue"
        
        setupToolbar()
        setupRoomTypeSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // The navigation back handling is automatically handled by the activity
    }

    private fun setupRoomTypeSpinner() {
        val roomTypes = listOf(
            "Private Room",
            "Semi-Private Room", 
            "Open Stage",
            "VIP Room",
            "Party Room",
            "Booth",
            "Bar Seating",
            "Other"
        )
        
        roomTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roomTypes
        )
        binding.roomTypeInput.setAdapter(roomTypeAdapter)
        
        binding.roomTypeInput.setOnItemClickListener { _, _, position, _ ->
            val selectedType = roomTypes[position]
            viewModel.setRoomType(selectedType)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.venue.collect { venue ->
                venue?.let {
                    (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Venue: ${it.name}"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.venueName.collect { name ->
                if (binding.venueNameInput.text.toString() != name) {
                    binding.venueNameInput.setText(name)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.address.collect { address ->
                val addressText = address ?: ""
                if (binding.venueAddressInput.text.toString() != addressText) {
                    binding.venueAddressInput.setText(addressText)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.roomType.collect { roomType ->
                val roomTypeText = roomType ?: ""
                if (binding.roomTypeInput.text.toString() != roomTypeText) {
                    binding.roomTypeInput.setText(roomTypeText, false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cost.collect { cost ->
                val costText = cost ?: ""
                if (binding.costInput.text.toString() != costText) {
                    binding.costInput.setText(costText)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hours.collect { hours ->
                val hoursText = hours ?: ""
                if (binding.hoursInput.text.toString() != hoursText) {
                    binding.hoursInput.setText(hoursText)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                val notesText = notes ?: ""
                if (binding.notesInput.text.toString() != notesText) {
                    binding.notesInput.setText(notesText)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songsAssociated.collect { count ->
                binding.songsAssociatedValue.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalVisits.collect { count ->
                binding.totalVisitsValue.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastVisited.collect { date ->
                binding.lastVisitedValue.text = date ?: getString(R.string.never_visited)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaving.collect { isSaving ->
                binding.saveButton.isEnabled = !isSaving
                binding.deleteButton.isEnabled = !isSaving
                binding.saveButton.text = if (isSaving) "Saving..." else getString(R.string.save_changes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveResult.collect { result ->
                result?.let {
                    if (it) {
                        Toast.makeText(requireContext(), "Venue updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update venue", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearSaveResult()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteResult.collect { result ->
                result?.let {
                    if (it) {
                        Toast.makeText(requireContext(), "Venue deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete venue", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearDeleteResult()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasUnsavedChanges.collect { hasChanges ->
                binding.saveButton.alpha = if (hasChanges) 1.0f else 0.5f
            }
        }
    }

    private fun setupClickListeners() {
        binding.venueNameInput.addTextChangedListener { text ->
            viewModel.setVenueName(text.toString())
        }

        binding.venueAddressInput.addTextChangedListener { text ->
            viewModel.setAddress(text.toString())
        }

        binding.costInput.addTextChangedListener { text ->
            viewModel.setCost(text.toString())
        }

        binding.hoursInput.addTextChangedListener { text ->
            viewModel.setHours(text.toString())
        }

        binding.notesInput.addTextChangedListener { text ->
            viewModel.setNotes(text.toString())
        }

        binding.saveButton.setOnClickListener {
            viewModel.saveVenue()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showUnsavedChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to save them before leaving?")
            .setPositiveButton("Save") { _, _ ->
                viewModel.saveVenue()
            }
            .setNegativeButton("Discard") { _, _ ->
                findNavController().navigateUp()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_venue_title))
            .setMessage(getString(R.string.confirm_delete_venue_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteVenue()
            }
            .setNegativeButton(getString(R.string.keep), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}