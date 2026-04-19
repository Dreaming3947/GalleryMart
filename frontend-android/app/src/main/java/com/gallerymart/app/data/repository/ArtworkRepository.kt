package com.gallerymart.app.data.repository

import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.data.remote.dto.ArtworkResponseDto
import com.gallerymart.app.feature.home.model.ArtworkUiModel

class ArtworkRepository(
    private val api: ArtworkApi = NetworkModule.createApi(ArtworkApi::class.java)
) {
    suspend fun getFeaturedArtworks(): List<ArtworkUiModel> {
        val response = api.searchArtworks(page = 0, size = 12)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "API request failed")
        }
        val page = response.data ?: return emptyList()

        return page.items.mapIndexed { index, dto ->
            ArtworkUiModel(
                id = dto.id,
                title = dto.title,
                author = dto.sellerName ?: "Unknown artist",
                priceText = dto.price.stripTrailingZeros().toPlainString() + " dM",
                ratingText = listOf("4.8", "4.7", "4.9", "4.6")[index % 4],
                imageUrl = dto.imageUrl ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800",
                badge = if (index % 3 == 0) "HOT" else ""
            )
        }
    }

    suspend fun getArtworkById(id: Long): ArtworkResponseDto {
        val response = api.getArtworkById(id)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "API request failed")
        }
        return response.data ?: throw IllegalStateException("Artwork not found")
    }
}

