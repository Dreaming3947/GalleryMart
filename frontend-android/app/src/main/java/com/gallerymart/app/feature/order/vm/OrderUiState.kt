package com.gallerymart.app.feature.order.vm

import com.gallerymart.app.data.remote.dto.response.OrderResponseDto

sealed class OrderUiState<out T> {
    object Idle : OrderUiState<Nothing>()
    object Loading : OrderUiState<Nothing>()
    data class Success<T>(val data: T) : OrderUiState<T>()
    data class Error(val message: String) : OrderUiState<Nothing>()
}

data class OrderListState(
    val ordersState: OrderUiState<List<OrderResponseDto>> = OrderUiState.Idle
)

data class OrderDetailState(
    val orderState: OrderUiState<OrderResponseDto> = OrderUiState.Idle,
    val paymentActionState: OrderUiState<Unit> = OrderUiState.Idle
)
