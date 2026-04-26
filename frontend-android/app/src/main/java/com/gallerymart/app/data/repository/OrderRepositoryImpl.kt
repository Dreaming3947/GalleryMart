package com.gallerymart.app.data.repository

import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.data.remote.api.OrderApi
import com.gallerymart.app.data.remote.dto.ApiResponseDto
import com.gallerymart.app.data.remote.dto.request.OrderCreateRequestDto
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import org.json.JSONObject
import retrofit2.HttpException

class OrderRepositoryImpl(
    private val api: OrderApi = NetworkModule.createApi(OrderApi::class.java)
) {
    suspend fun createOrder(artworkId: Long, note: String?): ApiResponseDto<OrderResponseDto> {
        return try {
            api.createOrder(OrderCreateRequestDto(artworkId, note?.trim()?.ifBlank { null }))
        } catch (error: HttpException) {
            throw IllegalStateException(parseApiErrorMessage(error, "Create order failed"))
        }
    }

    suspend fun markPaymentSent(orderId: Long): ApiResponseDto<OrderResponseDto> {
        return api.markPaymentSent(orderId)
    }

    suspend fun getMyOrders(): ApiResponseDto<List<OrderResponseDto>> {
        return api.getMyOrders()
    }

    suspend fun getOrderDetails(orderId: Long): ApiResponseDto<OrderResponseDto> {
        return api.getOrderById(orderId)
    }

    private fun parseApiErrorMessage(error: HttpException, fallback: String): String {
        val raw = error.response()?.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return fallback

        return runCatching {
            val root = JSONObject(raw)
            val message = root.optString("message")
            if (message.isNotBlank()) message else fallback
        }.getOrDefault(fallback)
    }
}
