package com.pseddev.singventory.ui.songs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.data.entity.Song
import com.pseddev.singventory.databinding.ItemSongBinding

class SongsAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onAddVenueClick: (Song) -> Unit,
    private val onEditSongClick: (Song) -> Unit
) : ListAdapter<Song, SongsAdapter.SongViewHolder>(SongDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding, onSongClick, onAddVenueClick, onEditSongClick)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class SongViewHolder(
        private val binding: ItemSongBinding,
        private val onSongClick: (Song) -> Unit,
        private val onAddVenueClick: (Song) -> Unit,
        private val onEditSongClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(song: Song) {
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
                
                // Click listeners
                root.setOnClickListener { onSongClick(song) }
                btnAddVenue.setOnClickListener { onAddVenueClick(song) }
                btnEditSong.setOnClickListener { onEditSongClick(song) }
            }
        }
    }
    
    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}