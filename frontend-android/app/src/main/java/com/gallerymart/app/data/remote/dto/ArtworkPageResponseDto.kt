package com.gallerymart.app.data.remote.dto

data class ArtworkPageResponseDto(
    val items: List<ArtworkResponseDto>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)

