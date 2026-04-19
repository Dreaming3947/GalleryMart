package com.gallerymart.app.feature.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gallerymart.app.data.repository.ArtworkRepository
import com.gallerymart.app.feature.home.model.ArtworkUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ArtworkRepository = ArtworkRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadArtworks()
    }

    fun loadArtworks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                repository.getFeaturedArtworks()
            }.onSuccess { artworks ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    artworks = artworks
                )
            }.onFailure { throwable ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Khong the ket noi backend. Dang hien du lieu mau.",
                    artworks = fallbackArtworks()
                )
            }
        }
    }

    private fun fallbackArtworks(): List<ArtworkUiModel> = listOf(
        ArtworkUiModel(
            id = "1",
            title = "Binh Minh Tren Bien",
            author = "Nguyen Van A",
            priceText = "16",
            ratingText = "4.8",
            imageUrl = "https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=800&q=80",
            badge = "HOT"
        ),
        ArtworkUiModel(
            id = "2",
            title = "Fragmented Identity",
            author = "Sarah Jenkins",
            priceText = "24",
            ratingText = "4.9",
            imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80",
            badge = "AUCTION"
        ),
        ArtworkUiModel(
            id = "3",
            title = "Silent Horizon",
            author = "Minh Tran",
            priceText = "19",
            ratingText = "4.7",
            imageUrl = "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?auto=format&fit=crop&w=800&q=80",
            badge = ""
        ),
        ArtworkUiModel(
            id = "4",
            title = "After The Rain",
            author = "Linh Hoang",
            priceText = "21",
            ratingText = "4.8",
            imageUrl = "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?auto=format&fit=crop&w=800&q=80",
            badge = ""
        )
    )
}
