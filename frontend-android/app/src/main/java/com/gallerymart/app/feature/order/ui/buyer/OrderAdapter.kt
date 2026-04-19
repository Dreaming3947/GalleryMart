package com.gallerymart.app.feature.order.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gallerymart.app.R
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import com.gallerymart.app.databinding.ItemOrderBinding

class OrderAdapter(
    private val onDetailClick: (OrderResponseDto) -> Unit
) : ListAdapter<OrderResponseDto, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderResponseDto) {
            binding.tvOrderId.text = "#ORD-${order.id}"
            binding.tvOrderStatus.text = order.status.uppercase()
            
            val statusColor = when (order.status.uppercase()) {
                "PENDING" -> R.color.status_pending
                "CONFIRMED" -> R.color.status_confirmed
                "CANCELLED" -> R.color.status_cancelled
                else -> R.color.text_secondary
            }
            binding.tvOrderStatus.setTextColor(ContextCompat.getColor(binding.root.context, statusColor))
            
            binding.tvArtworkTitle.text = order.artworkTitle ?: "Unknown Artwork"
            binding.tvSellerName.text = "Tác giả: ${order.sellerName ?: "N/A"}"
            binding.tvOrderTotal.text = "${order.totalPrice} dM"
            
            // Note: Load image using Glide/Coil here if needed
            // Glide.with(binding.ivArtwork).load(order.artworkImageUrl).into(binding.ivArtwork)

            binding.btnDetails.setOnClickListener { onDetailClick(order) }
            binding.root.setOnClickListener { onDetailClick(order) }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderResponseDto>() {
        override fun areItemsTheSame(oldItem: OrderResponseDto, newItem: OrderResponseDto): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: OrderResponseDto, newItem: OrderResponseDto): Boolean {
            return oldItem == newItem
        }
    }
}
