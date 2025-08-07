package com.pseddev.singventory.ui.association

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.databinding.ItemSongForAssociationBinding

data class SongAssociationItem(
    val song: Song,
    val existingAssociation: SongVenueInfo? = null,
    var isSelected: Boolean = false
)

class SongAssociationAdapter(
    private val onSongSelect: (SongAssociationItem, Boolean) -> Unit,
    private val onSingleAssociate: (SongAssociationItem) -> Unit
) : ListAdapter<SongAssociationItem, SongAssociationAdapter.SongAssociationViewHolder>(SongAssociationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAssociationViewHolder {
        val binding = ItemSongForAssociationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongAssociationViewHolder(binding, onSongSelect, onSingleAssociate)
    }

    override fun onBindViewHolder(holder: SongAssociationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedItems(): List<SongAssociationItem> {
        return currentList.filter { it.isSelected }
    }

    fun clearSelections() {
        val updatedList = currentList.map { it.copy(isSelected = false) }
        submitList(updatedList)
    }

    class SongAssociationViewHolder(
        private val binding: ItemSongForAssociationBinding,
        private val onSongSelect: (SongAssociationItem, Boolean) -> Unit,
        private val onSingleAssociate: (SongAssociationItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SongAssociationItem) {
            val song = item.song
            val association = item.existingAssociation

            binding.apply {
                songTitle.text = song.name
                songArtist.text = if (song.artist.isBlank()) "Artist not specified" else song.artist

                // Show key information if available
                val keyInfo = when {
                    song.preferredKey != null && song.referenceKey != null -> {
                        "Preferred: ${song.preferredKey} (Reference: ${song.referenceKey})"
                    }
                    song.preferredKey != null -> "Key: ${song.preferredKey}"
                    song.referenceKey != null -> "Reference: ${song.referenceKey}"
                    else -> "No key information"
                }
                songKeyInfo.text = keyInfo

                // Show performance count
                performanceCount.text = if (song.totalPerformances > 0) {
                    "${song.totalPerformances} performance${if (song.totalPerformances == 1) "" else "s"}"
                } else {
                    "Never performed"
                }

                // Show last performed date if available
                lastPerformed.text = song.lastPerformed?.let { timestamp ->
                    "Last: ${android.text.format.DateFormat.getDateFormat(root.context).format(timestamp)}"
                } ?: ""

                // Show association status if already associated
                if (association != null) {
                    associationStatus.visibility = View.VISIBLE
                    val venueInfo = buildString {
                        append("Already associated")
                        association.venuesSongId?.let { append(" • Venue ID: $it") }
                        association.venueKey?.let { append(" • Key: $it") }
                        if (association.keyAdjustment != 0) {
                            val direction = if (association.keyAdjustment > 0) "+" else ""
                            append(" • Adjust: $direction${association.keyAdjustment}")
                        }
                    }
                    associationStatus.text = venueInfo
                    
                    // Change button text for already associated songs
                    btnAssociateSingle.text = "Update"
                } else {
                    associationStatus.visibility = View.GONE
                    btnAssociateSingle.text = "Associate"
                }

                // Set checkbox state
                checkboxSelect.isChecked = item.isSelected

                // Set up click listeners
                checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (item.isSelected != isChecked) {
                        onSongSelect(item, isChecked)
                    }
                }

                // Card click should toggle selection
                root.setOnClickListener {
                    val newState = !item.isSelected
                    checkboxSelect.isChecked = newState
                    onSongSelect(item, newState)
                }

                btnAssociateSingle.setOnClickListener {
                    onSingleAssociate(item)
                }
            }
        }
    }

    private class SongAssociationDiffCallback : DiffUtil.ItemCallback<SongAssociationItem>() {
        override fun areItemsTheSame(oldItem: SongAssociationItem, newItem: SongAssociationItem): Boolean {
            return oldItem.song.id == newItem.song.id
        }

        override fun areContentsTheSame(oldItem: SongAssociationItem, newItem: SongAssociationItem): Boolean {
            return oldItem == newItem
        }
    }
}