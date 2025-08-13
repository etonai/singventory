package com.pseddev.singventory.ui.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.databinding.ItemVenueSongBinding

class VenueSongsAdapter(
    private val onSongClick: (SongVenueInfoWithDetails) -> Unit,
    private val onAddPerformance: ((SongVenueInfoWithDetails) -> Unit)? = null
) : ListAdapter<SongVenueInfoWithDetails, VenueSongsAdapter.VenueSongViewHolder>(VenueSongDiffCallback()) {
    
    private var hasVisitContext: Boolean = false
    
    fun updateActiveVisitStatus(hasVisitContext: Boolean) {
        this.hasVisitContext = hasVisitContext
        notifyDataSetChanged() // Update button visibility for all items
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueSongViewHolder {
        val binding = ItemVenueSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VenueSongViewHolder(binding, onSongClick, onAddPerformance)
    }
    
    override fun onBindViewHolder(holder: VenueSongViewHolder, position: Int) {
        holder.bind(getItem(position), hasVisitContext)
    }
    
    class VenueSongViewHolder(
        private val binding: ItemVenueSongBinding,
        private val onSongClick: (SongVenueInfoWithDetails) -> Unit,
        private val onAddPerformance: ((SongVenueInfoWithDetails) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(songVenueInfoWithDetails: SongVenueInfoWithDetails, hasVisitContext: Boolean) {
            val songVenueInfo = songVenueInfoWithDetails.songVenueInfo
            
            binding.songTitle.text = songVenueInfoWithDetails.songName
            binding.artistName.text = songVenueInfoWithDetails.artistName
            
            // Display venue song number if available
            val venueNumber = songVenueInfo.venuesSongId
            if (!venueNumber.isNullOrBlank()) {
                binding.venueSongNumber.text = "#$venueNumber"
                binding.venueSongNumber.visibility = android.view.View.VISIBLE
            } else {
                binding.venueSongNumber.visibility = android.view.View.GONE
            }
            
            // Display key adjustment if any
            val keyAdjustment = songVenueInfo.keyAdjustment
            if (keyAdjustment != 0 && !SongVenueInfo.isKeyAdjustmentUnknown(keyAdjustment)) {
                val keyText = if (keyAdjustment > 0) {
                    "Key: +$keyAdjustment"
                } else {
                    "Key: $keyAdjustment"
                }
                binding.keyAdjustment.text = keyText
                binding.keyAdjustment.visibility = android.view.View.VISIBLE
            } else {
                binding.keyAdjustment.visibility = android.view.View.GONE
            }
            
            // Display performance count
            val performanceText = when (songVenueInfoWithDetails.performanceCount) {
                0 -> "Never performed"
                1 -> "1 performance"
                else -> "${songVenueInfoWithDetails.performanceCount} performances"
            }
            binding.performanceCount.text = performanceText
            
            // Display venue key if different from original
            val venueKey = songVenueInfo.venueKey
            if (!venueKey.isNullOrBlank()) {
                binding.venueKey.text = "Venue key: $venueKey"
                binding.venueKey.visibility = android.view.View.VISIBLE
            } else {
                binding.venueKey.visibility = android.view.View.GONE
            }
            
            // Handle performance button visibility and clicks
            if (hasVisitContext && onAddPerformance != null) {
                binding.addPerformanceButton.visibility = android.view.View.VISIBLE
                binding.addPerformanceButton.setOnClickListener {
                    onAddPerformance.invoke(songVenueInfoWithDetails)
                }
            } else {
                binding.addPerformanceButton.visibility = android.view.View.GONE
                binding.addPerformanceButton.setOnClickListener(null)
            }
            
            // Handle song item clicks
            binding.root.setOnClickListener {
                onSongClick(songVenueInfoWithDetails)
            }
        }
    }
    
    private class VenueSongDiffCallback : DiffUtil.ItemCallback<SongVenueInfoWithDetails>() {
        override fun areItemsTheSame(oldItem: SongVenueInfoWithDetails, newItem: SongVenueInfoWithDetails): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: SongVenueInfoWithDetails, newItem: SongVenueInfoWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}