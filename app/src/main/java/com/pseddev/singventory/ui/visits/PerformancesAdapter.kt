package com.pseddev.singventory.ui.visits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.databinding.ItemPerformanceBinding
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent a performance with song details
data class PerformanceWithSong(
    val performance: com.pseddev.singventory.data.entity.Performance,
    val songName: String,
    val artistName: String
)

class PerformancesAdapter(
    private val onPerformanceClick: (PerformanceWithSong) -> Unit
) : ListAdapter<PerformanceWithSong, PerformancesAdapter.PerformanceViewHolder>(PerformanceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceViewHolder {
        val binding = ItemPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PerformanceViewHolder(binding, onPerformanceClick)
    }
    
    override fun onBindViewHolder(holder: PerformanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class PerformanceViewHolder(
        private val binding: ItemPerformanceBinding,
        private val onPerformanceClick: (PerformanceWithSong) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        fun bind(performanceWithSong: PerformanceWithSong) {
            val performance = performanceWithSong.performance
            
            // Format performance time
            val performanceTime = Date(performance.timestamp)
            binding.performanceTime.text = timeFormat.format(performanceTime)
            
            // Song and artist information
            binding.songTitle.text = performanceWithSong.songName
            binding.artistName.text = if (performanceWithSong.artistName.isBlank()) {
                "Artist not specified"
            } else {
                performanceWithSong.artistName
            }
            
            // Key adjustment display
            if (performance.keyAdjustment != 0) {
                binding.keyAdjustment.visibility = View.VISIBLE
                val adjustmentText = if (performance.keyAdjustment > 0) {
                    "+${performance.keyAdjustment}"
                } else {
                    performance.keyAdjustment.toString()
                }
                binding.keyAdjustment.text = adjustmentText
            } else {
                binding.keyAdjustment.visibility = View.GONE
            }
            
            // Performance notes
            if (!performance.notes.isNullOrBlank()) {
                binding.performanceNotes.visibility = View.VISIBLE
                binding.performanceNotes.text = performance.notes
            } else {
                binding.performanceNotes.visibility = View.GONE
            }
            
            // Handle click
            binding.root.setOnClickListener {
                onPerformanceClick(performanceWithSong)
            }
        }
    }
    
    private class PerformanceDiffCallback : DiffUtil.ItemCallback<PerformanceWithSong>() {
        override fun areItemsTheSame(oldItem: PerformanceWithSong, newItem: PerformanceWithSong): Boolean {
            return oldItem.performance.id == newItem.performance.id
        }
        
        override fun areContentsTheSame(oldItem: PerformanceWithSong, newItem: PerformanceWithSong): Boolean {
            return oldItem == newItem
        }
    }
}