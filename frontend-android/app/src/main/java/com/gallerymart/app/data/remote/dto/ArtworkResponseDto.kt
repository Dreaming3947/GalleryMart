package com.gallerymart.app.data.remote.dto

import java.math.BigDecimal

data class ArtworkResponseDto(
    val id: Long,
    val sellerId: Long?,
    val sellerName: String?,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    val imageUrl: String?,
    val status: String?,
    val category: String?
)

