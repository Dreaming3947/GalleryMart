package com.gallerymart.app.feature.artwork.ui.seller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallerymart.app.databinding.ItemSellerArtworkBinding
import com.gallerymart.app.feature.artwork.model.Artwork
import java.text.NumberFormat
import java.util.*

class SellerInventoryAdapter(
    private val onEditClick: (Artwork) -> Unit,
    private val onDeleteClick: (Artwork) -> Unit
) : ListAdapter<Artwork, SellerInventoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSellerArtworkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSellerArtworkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(artwork: Artwork) {
            binding.apply {
                tvTitle.text = artwork.title
                tvPrice.text = formatCurrency(artwork.price)
                tvStatus.text = artwork.status ?: "AVAILABLE"
                
                Glide.with(ivArtwork.context)
                    .load(artwork.imageUrl)
                    .centerCrop()
                    .into(ivArtwork)

                btnMore.setOnClickListener {
                    val popup = android.widget.PopupMenu(it.context, it)
                    popup.menu.add("Chỉnh sửa")
                    popup.menu.add("Xóa")
                    
                    popup.setOnMenuItemClickListener { item ->
                        when (item.title) {
                            "Chỉnh sửa" -> onEditClick(artwork)
                            "Xóa" -> {
                                androidx.appcompat.app.AlertDialog.Builder(it.context)
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa tác phẩm này?")
                                    .setPositiveButton("Xóa") { _, _ -> onDeleteClick(artwork) }
                                    .setNegativeButton("Hủy", null)
                                    .show()
                            }
                        }
                        true
                    }
                    popup.show()
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            return format.format(amount)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Artwork>() {
        override fun areItemsTheSame(oldItem: Artwork, newItem: Artwork): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Artwork, newItem: Artwork): Boolean =
            oldItem == newItem
    }
}
