package com.gallerymart.app.feature.home.vm

import com.gallerymart.app.feature.home.model.ArtworkUiModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val artworks: List<ArtworkUiModel> = emptyList()
)

