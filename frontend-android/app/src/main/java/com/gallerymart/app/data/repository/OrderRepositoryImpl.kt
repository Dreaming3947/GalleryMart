package com.gallerymart.app.data.repository

import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.data.remote.api.OrderApi
import com.gallerymart.app.data.remote.dto.ApiResponseDto
import com.gallerymart.app.data.remote.dto.request.OrderCreateRequestDto
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto

class OrderRepositoryImpl(
    private val api: OrderApi = NetworkModule.createApi(OrderApi::class.java)
) {
    suspend fun createOrder(artworkId: Long, note: String?): ApiResponseDto<OrderResponseDto> {
        return api.createOrder(OrderCreateRequestDto(artworkId, note))
    }

    suspend fun markPaymentSent(orderId: Long): ApiResponseDto<OrderResponseDto> {
        return api.markPaymentSent(orderId)
    }

    suspend fun getMyOrders(): ApiResponseDto<List<OrderResponseDto>> {
        return api.getMyOrders()
    }

    suspend fun getOrderDetails(orderId: Long): ApiResponseDto<OrderResponseDto> {
        return api.getOrderDetails(orderId)
    }
}
