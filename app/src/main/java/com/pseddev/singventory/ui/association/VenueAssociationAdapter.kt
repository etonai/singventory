package com.pseddev.singventory.ui.association

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.singventory.data.entity.SongVenueInfo
import com.pseddev.singventory.data.entity.Venue
import com.pseddev.singventory.databinding.ItemVenueForAssociationBinding

data class VenueAssociationItem(
    val venue: Venue,
    val existingAssociation: SongVenueInfo? = null,
    var isSelected: Boolean = false
)

class VenueAssociationAdapter(
    private val onVenueSelect: (VenueAssociationItem, Boolean) -> Unit,
    private val onSingleAssociate: (VenueAssociationItem) -> Unit
) : ListAdapter<VenueAssociationItem, VenueAssociationAdapter.VenueAssociationViewHolder>(VenueAssociationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueAssociationViewHolder {
        val binding = ItemVenueForAssociationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VenueAssociationViewHolder(binding, onVenueSelect, onSingleAssociate)
    }

    override fun onBindViewHolder(holder: VenueAssociationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedItems(): List<VenueAssociationItem> {
        return currentList.filter { it.isSelected }
    }

    fun clearSelections() {
        val updatedList = currentList.map { it.copy(isSelected = false) }
        submitList(updatedList)
    }

    class VenueAssociationViewHolder(
        private val binding: ItemVenueForAssociationBinding,
        private val onVenueSelect: (VenueAssociationItem, Boolean) -> Unit,
        private val onSingleAssociate: (VenueAssociationItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VenueAssociationItem) {
            val venue = item.venue
            val association = item.existingAssociation

            binding.apply {
                venueName.text = venue.name
                venueAddress.text = venue.address ?: "Address not specified"

                // Show visit count
                val visitText = if (venue.totalVisits == 1) {
                    "1 visit"
                } else {
                    "${venue.totalVisits} visits"
                }
                visitCount.text = visitText

                // Show last visited date if available
                lastVisited.text = venue.lastVisited?.let { timestamp ->
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
                    
                    // Change button text for already associated venues
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
                        onVenueSelect(item, isChecked)
                    }
                }

                // Card click should toggle selection
                root.setOnClickListener {
                    val newState = !item.isSelected
                    checkboxSelect.isChecked = newState
                    onVenueSelect(item, newState)
                }

                btnAssociateSingle.setOnClickListener {
                    onSingleAssociate(item)
                }
            }
        }
    }

    private class VenueAssociationDiffCallback : DiffUtil.ItemCallback<VenueAssociationItem>() {
        override fun areItemsTheSame(oldItem: VenueAssociationItem, newItem: VenueAssociationItem): Boolean {
            return oldItem.venue.id == newItem.venue.id
        }

        override fun areContentsTheSame(oldItem: VenueAssociationItem, newItem: VenueAssociationItem): Boolean {
            return oldItem == newItem
        }
    }
}