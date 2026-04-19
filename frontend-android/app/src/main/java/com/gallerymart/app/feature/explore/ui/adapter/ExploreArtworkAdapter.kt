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

    inner class ExploreArtworkViewHolder(
        private val binding: ItemExploreArtworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ArtworkUiModel) {
            binding.itemTitle.text = item.title
            binding.itemAuthor.text = item.author
            binding.itemPrice.text = item.priceText
            binding.itemBadge.text = item.badge
            binding.itemBadge.visibility = if (item.badge.isBlank()) View.GONE else View.VISIBLE
            binding.itemImage.load(item.imageUrl)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<ArtworkUiModel>() {
        override fun areItemsTheSame(oldItem: ArtworkUiModel, newItem: ArtworkUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArtworkUiModel, newItem: ArtworkUiModel): Boolean {
            return oldItem == newItem
        }
    }
}

