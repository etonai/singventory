package com.pseddev.singventory.ui.songs

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
import com.pseddev.singventory.data.entity.KeyUtils
import com.pseddev.singventory.data.entity.MusicalKey
import com.pseddev.singventory.data.repository.SingventoryRepository
import com.pseddev.singventory.databinding.FragmentAddSongBinding
import kotlinx.coroutines.launch

class AddSongFragment : Fragment() {
    
    private var _binding: FragmentAddSongBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddSongViewModel by viewModels {
        val database = SingventoryDatabase.getDatabase(requireContext())
        AddSongViewModel.Factory(
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
        _binding = FragmentAddSongBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupKeyDropdowns()
        setupFormValidation()
        setupSaveButton()
        setupObservers()
    }
    
    private fun setupKeyDropdowns() {
        val keyOptions = listOf("") + MusicalKey.values().map { it.displayName }
        val keyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, keyOptions)
        
        binding.referenceKeyInput.setAdapter(keyAdapter)
        binding.preferredKeyInput.setAdapter(keyAdapter)
        
        binding.referenceKeyInput.setOnItemClickListener { _, _, position, _ ->
            val selectedKey = if (position == 0) null else MusicalKey.values()[position - 1]
            viewModel.setReferenceKey(selectedKey)
        }
        
        binding.preferredKeyInput.setOnItemClickListener { _, _, position, _ ->
            val selectedKey = if (position == 0) null else MusicalKey.values()[position - 1]
            viewModel.setPreferredKey(selectedKey)
        }
    }
    
    private fun setupFormValidation() {
        binding.songNameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setSongName(text?.toString() ?: "")
            clearError(binding.songNameLayout)
        }
        
        binding.artistInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setArtist(text?.toString() ?: "")
            clearError(binding.artistLayout)
        }
        
        binding.lyricsInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setLyrics(text?.toString()?.takeIf { it.isNotBlank() })
        }
    }
    
    private fun setupSaveButton() {
        binding.fabSaveSong.setOnClickListener {
            if (validateForm()) {
                viewModel.saveSong()
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSaving.collect { isSaving ->
                    binding.fabSaveSong.isEnabled = !isSaving
                    if (isSaving) {
                        binding.fabSaveSong.text = "Saving..."
                    } else {
                        binding.fabSaveSong.text = getString(R.string.save_song)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result?.let { success ->
                        if (success) {
                            Snackbar.make(binding.root, "Song saved successfully!", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Snackbar.make(binding.root, "Failed to save song. Please try again.", Snackbar.LENGTH_LONG).show()
                        }
                        viewModel.clearSaveResult()
                    }
                }
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        val songName = binding.songNameInput.text?.toString()?.trim()
        if (songName.isNullOrBlank()) {
            binding.songNameLayout.error = "Song name is required"
            isValid = false
        }
        
        // Artist is now optional - no validation required
        
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