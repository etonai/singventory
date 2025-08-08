package com.pseddev.singventory.ui.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.databinding.ItemVenueBinding

class VenuesAdapter(
    private val onVenueClick: (Venue) -> Unit,
    private val onViewSongsClick: (Venue) -> Unit,
    private val onAddSongToVenueClick: (Venue) -> Unit,
    private val onEditVenueClick: (Venue) -> Unit
) : ListAdapter<Venue, VenuesAdapter.VenueViewHolder>(VenueDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueViewHolder {
        val binding = ItemVenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VenueViewHolder(binding, onVenueClick, onViewSongsClick, onAddSongToVenueClick, onEditVenueClick)
    }
    
    override fun onBindViewHolder(holder: VenueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class VenueViewHolder(
        private val binding: ItemVenueBinding,
        private val onVenueClick: (Venue) -> Unit,
        private val onViewSongsClick: (Venue) -> Unit,
        private val onAddSongToVenueClick: (Venue) -> Unit,
        private val onEditVenueClick: (Venue) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(venue: Venue) {
            binding.venueName.text = venue.name
            binding.venueLocation.text = venue.address ?: "Address not specified"
            
            // Show visit count - PlayStreak pattern of showing performance metrics
            val visitText = if (venue.totalVisits == 1) {
                "1 visit"
            } else {
                "${venue.totalVisits} visits"
            }
            binding.venueVisitCount.text = visitText
            
            // Set up click listeners
            binding.root.setOnClickListener {
                onVenueClick(venue)
            }
            
            binding.btnViewSongs.setOnClickListener {
                onViewSongsClick(venue)
            }
            
            binding.btnAddSongToVenue.setOnClickListener {
                onAddSongToVenueClick(venue)
            }
            
            binding.btnEditVenue.setOnClickListener {
                onEditVenueClick(venue)
            }
        }
    }
    
    private class VenueDiffCallback : DiffUtil.ItemCallback<Venue>() {
        override fun areItemsTheSame(oldItem: Venue, newItem: Venue): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Venue, newItem: Venue): Boolean {
            return oldItem == newItem
        }
    }
}