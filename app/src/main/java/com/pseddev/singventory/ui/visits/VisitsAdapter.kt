package com.pseddev.singventory.ui.visits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.databinding.ItemVisitBinding
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent a visit with additional display information
data class VisitWithDetails(
    val visit: com.pseddev.singventory.data.entity.Visit,
    val venueName: String,
    val performanceCount: Int
)

class VisitsAdapter(
    private val onVisitClick: (VisitWithDetails) -> Unit,
    private val onResumeVisitClick: (VisitWithDetails) -> Unit,
    private val onEndVisitClick: (VisitWithDetails) -> Unit
) : ListAdapter<VisitWithDetails, VisitsAdapter.VisitViewHolder>(VisitDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val binding = ItemVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitViewHolder(binding, onVisitClick, onResumeVisitClick, onEndVisitClick)
    }
    
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class VisitViewHolder(
        private val binding: ItemVisitBinding,
        private val onVisitClick: (VisitWithDetails) -> Unit,
        private val onResumeVisitClick: (VisitWithDetails) -> Unit,
        private val onEndVisitClick: (VisitWithDetails) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        
        fun bind(visitWithDetails: VisitWithDetails) {
            val visit = visitWithDetails.visit
            
            binding.venueName.text = visitWithDetails.venueName
            
            // Format visit date and time
            val visitDate = Date(visit.timestamp)
            binding.visitDateTime.text = dateFormat.format(visitDate)
            
            // Show performance count and visit status
            val performanceText = if (visitWithDetails.performanceCount == 1) {
                "1 performance"
            } else {
                "${visitWithDetails.performanceCount} performances"
            }
            binding.performanceCount.text = performanceText
            
            // Show visit status and duration
            if (visit.endTimestamp != null) {
                binding.visitStatus.text = "Completed"
                val duration = (visit.endTimestamp - visit.timestamp) / (1000 * 60) // minutes
                binding.visitDuration.text = "${duration} min"
                
                // Hide action buttons for completed visits - click the visit itself to view/edit
                binding.btnResumeVisit.visibility = android.view.View.GONE
                binding.btnEndVisit.visibility = android.view.View.GONE
            } else {
                binding.visitStatus.text = "Active"
                binding.visitDuration.text = "Ongoing"
                
                // Show both action buttons for active visits
                binding.btnResumeVisit.visibility = android.view.View.VISIBLE
                binding.btnResumeVisit.text = "Resume"
                binding.btnEndVisit.visibility = android.view.View.VISIBLE
            }
            
            // Handle clicks
            binding.root.setOnClickListener {
                onVisitClick(visitWithDetails)
            }
            
            binding.btnResumeVisit.setOnClickListener {
                onResumeVisitClick(visitWithDetails)
            }
            
            binding.btnEndVisit.setOnClickListener {
                onEndVisitClick(visitWithDetails)
            }
        }
    }
    
    private class VisitDiffCallback : DiffUtil.ItemCallback<VisitWithDetails>() {
        override fun areItemsTheSame(oldItem: VisitWithDetails, newItem: VisitWithDetails): Boolean {
            return oldItem.visit.id == newItem.visit.id
        }
        
        override fun areContentsTheSame(oldItem: VisitWithDetails, newItem: VisitWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}