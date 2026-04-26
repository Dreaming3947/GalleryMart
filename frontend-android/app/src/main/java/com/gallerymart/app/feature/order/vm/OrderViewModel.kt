package com.gallerymart.app.feature.order.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import com.gallerymart.app.data.repository.OrderRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepositoryImpl = OrderRepositoryImpl()
) : ViewModel() {

    private val _listState = MutableStateFlow(OrderListState())
    val listState: StateFlow<OrderListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(OrderDetailState())
    val detailState: StateFlow<OrderDetailState> = _detailState.asStateFlow()

    private val _checkoutState = MutableStateFlow<OrderUiState<OrderResponseDto>>(OrderUiState.Idle)
    val checkoutState: StateFlow<OrderUiState<OrderResponseDto>> = _checkoutState.asStateFlow()

    fun createOrder(artworkId: Long, note: String?) {
        viewModelScope.launch {
            _checkoutState.value = OrderUiState.Loading
            try {
                val response = repository.createOrder(artworkId, note)
                if (response.success && response.data != null) {
                    _checkoutState.value = OrderUiState.Success(response.data)
                } else {
                    _checkoutState.value = OrderUiState.Error(response.message ?: "Failed to create order")
                }
            } catch (e: Exception) {
                _checkoutState.value = OrderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getMyOrders() {
        viewModelScope.launch {
            _listState.update { it.copy(ordersState = OrderUiState.Loading) }
            try {
                val response = repository.getMyOrders()
                if (response.success && response.data != null) {
                    _listState.update { it.copy(ordersState = OrderUiState.Success(response.data)) }
                } else {
                    _listState.update { it.copy(ordersState = OrderUiState.Error(response.message ?: "Failed to fetch orders")) }
                }
            } catch (e: Exception) {
                _listState.update { it.copy(ordersState = OrderUiState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun getOrderDetails(orderId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(orderState = OrderUiState.Loading) }
            try {
                val response = repository.getOrderDetails(orderId)
                if (response.success && response.data != null) {
                    _detailState.update { it.copy(orderState = OrderUiState.Success(response.data)) }
                } else {
                    _detailState.update { it.copy(orderState = OrderUiState.Error(response.message ?: "Failed to fetch order details")) }
                }
            } catch (e: Exception) {
                _detailState.update { it.copy(orderState = OrderUiState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun markPaymentSent(orderId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(paymentActionState = OrderUiState.Loading) }
            try {
                val response = repository.markPaymentSent(orderId)
                if (response.success) {
                    _detailState.update { it.copy(paymentActionState = OrderUiState.Success(Unit)) }
                    // Refresh order details to update UI state
                    getOrderDetails(orderId)
                } else {
                    _detailState.update { it.copy(paymentActionState = OrderUiState.Error(response.message ?: "Failed to mark payment")) }
                }
            } catch (e: Exception) {
                _detailState.update { it.copy(paymentActionState = OrderUiState.Error(e.message ?: "Unknown error")) }
            }
        }
    }
}
