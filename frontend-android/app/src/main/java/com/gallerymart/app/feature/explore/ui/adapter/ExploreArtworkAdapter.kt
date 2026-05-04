package com.gallerymart.app.feature.explore.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gallerymart.app.databinding.ItemExploreArtworkBinding
import com.gallerymart.app.feature.home.model.ArtworkUiModel

/**
 * Adapter hiển thị danh sách tác phẩm nghệ thuật trong màn hình Khám phá.
 * Sử dụng ListAdapter để tối ưu hóa việc cập nhật danh sách bằng DiffUtil.
 */
class ExploreArtworkAdapter(
    private val onClick: (ArtworkUiModel) -> Unit
) : ListAdapter<ArtworkUiModel, ExploreArtworkAdapter.ExploreArtworkViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreArtworkViewHolder {
        val binding = ItemExploreArtworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExploreArtworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreArtworkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder quản lý việc hiển thị dữ liệu của từng item tác phẩm.
     */
    inner class ExploreArtworkViewHolder(
        private val binding: ItemExploreArtworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Ánh xạ dữ liệu từ model vào các thành phần UI.
         */
        fun bind(item: ArtworkUiModel) {
            binding.itemTitle.text = item.title
            binding.itemAuthor.text = item.author
            binding.itemPrice.text = item.priceText
            binding.itemBadge.text = item.badge
            
            // Ẩn badge nếu không có nội dung
            binding.itemBadge.visibility = if (item.badge.isBlank()) View.GONE else View.VISIBLE
            
            // Tải hình ảnh bằng thư viện Coil
            binding.itemImage.load(item.imageUrl)
            
            // Xử lý sự kiện click vào item
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    /**
     * DiffUtil giúp so sánh danh sách cũ và mới để cập nhật UI hiệu quả.
     */
    private object Diff : DiffUtil.ItemCallback<ArtworkUiModel>() {
        override fun areItemsTheSame(oldItem: ArtworkUiModel, newItem: ArtworkUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArtworkUiModel, newItem: ArtworkUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
