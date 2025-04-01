package com.example.stockrequest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stockrequest.R
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.databinding.ItemStockRequestBinding
import java.text.SimpleDateFormat
import java.util.Locale

class RequestListAdapter(
    private val onItemClick: (StockRequest) -> Unit
) : ListAdapter<StockRequest, RequestListAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemStockRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemStockRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(request: StockRequest) {
            binding.apply {
                tvItemName.text = request.itemName
                tvQuantity.text = "Qty: ${request.quantityNeeded}"
                tvTimeline.text = "• ${request.daysNeeded} days"

                // Format date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = dateFormat.format(request.dateSubmitted)

                // Set status chip
                chipStatus.text = request.status

                // Set chip color based on status
                val chipColor = when (request.status) {
                    StockRequest.Status.SUBMITTED.displayName -> R.color.status_submitted
                    StockRequest.Status.PROCESSING.displayName -> R.color.status_processing
                    StockRequest.Status.ORDERED.displayName -> R.color.status_ordered
                    StockRequest.Status.COMPLETED.displayName -> R.color.status_completed
                    StockRequest.Status.ON_HOLD.displayName -> R.color.status_on_hold
                    else -> R.color.status_submitted
                }
                chipStatus.setChipBackgroundColorResource(chipColor)

                // Load image if available
                if (request.photoUrl.isNotEmpty()) {
                    Glide.with(ivItemPhoto.context)
                        .load(request.photoUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(ivItemPhoto)
                } else {
                    ivItemPhoto.setImageResource(R.drawable.placeholder_image)
                }
            }
        }
    }
}

class RequestDiffCallback : DiffUtil.ItemCallback<StockRequest>() {
    override fun areItemsTheSame(oldItem: StockRequest, newItem: StockRequest): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StockRequest, newItem: StockRequest): Boolean {
        return oldItem == newItem
    }
}