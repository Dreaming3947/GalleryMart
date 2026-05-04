/**
 * Tệp này định nghĩa giao diện (Interface) để kết nối với các API liên quan đến Đơn hàng (Orders).
 * Các chức năng bao gồm: Tạo đơn hàng, xác nhận thanh toán, hủy đơn và lấy lịch sử mua hàng.
 * AI sử dụng tệp này để biết cấu trúc URL và phương thức gửi dữ liệu lên server.
 */
package com.gallerymart.app.data.remote.api

import com.gallerymart.app.data.remote.dto.ApiResponseDto
import com.gallerymart.app.data.remote.dto.request.OrderCreateRequestDto
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import retrofit2.http.*

interface OrderApi {
    @POST("api/orders")
    suspend fun createOrder(@Body request: OrderCreateRequestDto): ApiResponseDto<OrderResponseDto>

    @PATCH("api/orders/{id}/payment-sent")
    suspend fun markPaymentSent(@Path("id") orderId: Long): ApiResponseDto<OrderResponseDto>

    @PATCH("api/orders/{id}/confirm")
    suspend fun confirmOrder(@Path("id") orderId: Long): ApiResponseDto<OrderResponseDto>

    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: Long): ApiResponseDto<OrderResponseDto>

    @GET("api/orders/my")
    suspend fun getMyOrders(): ApiResponseDto<List<OrderResponseDto>>

    @GET("api/orders/sales")
    suspend fun getSalesOrders(): ApiResponseDto<List<OrderResponseDto>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") orderId: Long): ApiResponseDto<OrderResponseDto>
}
