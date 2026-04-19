package com.gallerymart.app.feature.detail.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gallerymart.app.data.repository.ArtworkRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: ArtworkRepository = ArtworkRepository()
) : ViewModel() {

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming = _isConfirming.asStateFlow()

    private val _actionResult = MutableSharedFlow<Pair<Boolean, String>>()
    val actionResult = _actionResult.asSharedFlow()

    fun confirmOrder(artworkId: Long) {
        viewModelScope.launch {
            _isConfirming.value = true
            repository.confirmOrder(artworkId)
                .onSuccess {
                    _actionResult.emit(true to "Xác nhận đơn hàng thành công!")
                }
                .onFailure {
                    _actionResult.emit(false to "Lỗi: ${it.message}")
                }
            _isConfirming.value = false
        }
    }
}