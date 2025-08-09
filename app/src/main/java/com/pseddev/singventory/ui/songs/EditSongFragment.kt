package com.pseddev.singventory.ui.songs

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
import com.pseddev.singventory.data.entity.MusicalKey
import com.pseddev.singventory.databinding.FragmentEditSongBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditSongFragment : Fragment() {

    private var _binding: FragmentEditSongBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditSongViewModel by viewModels {
        val songId = arguments?.getLong("songId") ?: 0L
        val database = SingventoryDatabase.getDatabase(requireContext())
        EditSongViewModel.Factory(
            SingventoryRepository(
                database.songDao(),
                database.venueDao(),
                database.visitDao(),
                database.performanceDao(),
                database.songVenueInfoDao()
            ),
            songId
        )
    }

    private lateinit var referenceKeyAdapter: ArrayAdapter<String>
    private lateinit var preferredKeyAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set initial title while data loads
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Song"
        
        setupToolbar()
        setupKeySpinners()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // The navigation back handling is automatically handled by the activity
    }

    private fun setupKeySpinners() {
        val keyOptions = buildList {
            add("")  // Empty option for no key specified
            // Add all major keys first
            addAll(MusicalKey.getMajorKeys().map { it.displayName })
            // Add all minor keys second
            addAll(MusicalKey.getMinorKeys().map { it.displayName })
        }
        
        referenceKeyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            keyOptions
        )
        binding.referenceKeyInput.setAdapter(referenceKeyAdapter)
        
        preferredKeyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            keyOptions
        )
        binding.preferredKeyInput.setAdapter(preferredKeyAdapter)
        
        binding.referenceKeyInput.setOnItemClickListener { _, _, position, _ ->
            val selectedKey = if (position == 0) null else MusicalKey.fromDisplayName(keyOptions[position])
            viewModel.setReferenceKey(selectedKey)
        }
        
        binding.preferredKeyInput.setOnItemClickListener { _, _, position, _ ->
            val selectedKey = if (position == 0) null else MusicalKey.fromDisplayName(keyOptions[position])
            viewModel.setPreferredKey(selectedKey)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.song.collect { song ->
                song?.let {
                    (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Song: ${it.name}"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songName.collect { name ->
                if (binding.songNameInput.text.toString() != name) {
                    binding.songNameInput.setText(name)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.artist.collect { artist ->
                if (binding.artistInput.text.toString() != artist) {
                    binding.artistInput.setText(artist)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.referenceKey.collect { key ->
                val displayText = key?.displayName ?: ""
                if (binding.referenceKeyInput.text.toString() != displayText) {
                    binding.referenceKeyInput.setText(displayText, false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.preferredKey.collect { key ->
                val displayText = key?.displayName ?: ""
                if (binding.preferredKeyInput.text.toString() != displayText) {
                    binding.preferredKeyInput.setText(displayText, false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lyrics.collect { lyrics ->
                if (binding.lyricsInput.text.toString() != (lyrics ?: "")) {
                    binding.lyricsInput.setText(lyrics ?: "")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.venuesAssociated.collect { count ->
                binding.venuesAssociatedValue.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaving.collect { isSaving ->
                binding.fabSaveSong.isEnabled = !isSaving
                binding.btnDeleteSong.isEnabled = !isSaving
                binding.fabSaveSong.text = if (isSaving) "Saving..." else getString(R.string.save_changes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveResult.collect { result ->
                result?.let {
                    if (it) {
                        Toast.makeText(requireContext(), "Song updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update song", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearSaveResult()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteResult.collect { result ->
                result?.let {
                    if (it) {
                        Toast.makeText(requireContext(), "Song deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete song", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearDeleteResult()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasUnsavedChanges.collect { hasChanges ->
                binding.fabSaveSong.alpha = if (hasChanges) 1.0f else 0.5f
            }
        }
    }

    private fun setupClickListeners() {
        binding.songNameInput.addTextChangedListener { text ->
            viewModel.setSongName(text.toString())
        }

        binding.artistInput.addTextChangedListener { text ->
            viewModel.setArtist(text.toString())
        }

        binding.lyricsInput.addTextChangedListener { text ->
            viewModel.setLyrics(text.toString())
        }

        binding.fabSaveSong.setOnClickListener {
            viewModel.saveSong()
        }

        binding.btnDeleteSong.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showUnsavedChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to save them before leaving?")
            .setPositiveButton("Save") { _, _ ->
                viewModel.saveSong()
            }
            .setNegativeButton("Discard") { _, _ ->
                findNavController().navigateUp()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_song_title))
            .setMessage(getString(R.string.confirm_delete_song_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteSong()
            }
            .setNegativeButton(getString(R.string.keep), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}