package com.gallerymart.app.feature.home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gallerymart.app.R
import com.gallerymart.app.databinding.ItemArtworkBinding
import com.gallerymart.app.feature.home.model.ArtworkUiModel

/**
 * Adapter quản lý việc hiển thị danh sách tác phẩm nghệ thuật trên trang chủ.
 * Sử dụng ListAdapter để tối ưu hóa việc cập nhật danh sách (DiffUtil).
 */
class ArtworkAdapter(
    private val onClick: (ArtworkUiModel) -> Unit
) : ListAdapter<ArtworkUiModel, ArtworkAdapter.ArtworkViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val binding = ItemArtworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder giữ các thành phần giao diện cho mỗi mục tác phẩm.
     */
    inner class ArtworkViewHolder(
        private val binding: ItemArtworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ArtworkUiModel) {
            // Gán thông tin tác phẩm vào các View tương ứng
            binding.itemTitle.text = item.title
            binding.itemAuthor.text = binding.root.context.getString(R.string.author_by, item.author)
            binding.itemPrice.text = item.priceText
            binding.itemRating.text = binding.root.context.getString(R.string.rating_text, item.ratingText)
            
            // Hiển thị Badge (ví dụ: HOT, SOLD) nếu có
            binding.itemBadge.text = item.badge
            binding.itemBadge.visibility = if (item.badge.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
            
            // Tải ảnh tác phẩm bằng thư viện Coil
            binding.itemImage.load(item.imageUrl)
            
            // Hiệu ứng nhẹ khi nhấn vào nút yêu thích
            binding.btnFavorite.setOnClickListener { view ->
                view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(70).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
            }
            
            // Sự kiện click vào toàn bộ item
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    /**
     * Cơ chế so sánh dữ liệu để xác định các thay đổi trong danh sách.
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

