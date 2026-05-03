package com.gallerymart.app.data.repository

import com.gallerymart.app.BuildConfig
import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.data.remote.api.AuthApi
import com.gallerymart.app.data.remote.api.NotificationApi
import com.gallerymart.app.data.remote.api.OrderApi
import com.gallerymart.app.data.remote.dto.ArtworkCreateRequestDto
import com.gallerymart.app.data.remote.dto.ArtworkResponseDto
import com.gallerymart.app.data.remote.dto.AuthResponseDto
import com.gallerymart.app.data.remote.dto.LoginRequestDto
import com.gallerymart.app.data.remote.dto.RegisterRequestDto
import com.gallerymart.app.data.remote.dto.UpdateProfileRequestDto
import com.gallerymart.app.data.remote.dto.UserProfileResponseDto
import com.gallerymart.app.data.remote.dto.NotificationResponseDto
import com.gallerymart.app.data.remote.dto.request.OrderCreateRequestDto
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import com.gallerymart.app.feature.home.model.ArtworkUiModel
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Repository xử lý các nghiệp vụ liên quan đến tác phẩm nghệ thuật.
 * Giúp tách biệt logic dữ liệu khỏi lớp giao diện (UI).
 */
class ArtworkRepository(
    private val api: ArtworkApi = NetworkModule.artworkApi
) {
    /**
     * Lấy danh sách tác phẩm nổi bật để hiển thị trên trang chủ.
     * Chuyển đổi dữ liệu từ API (DTO) sang Model hiển thị (UiModel).
     */
    suspend fun getFeaturedArtworks(): List<ArtworkUiModel> {
        val response = api.searchArtworks(page = 0, size = 12)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Không thể tải danh sách tác phẩm")
        }
        val page = response.data ?: return emptyList()

        return page.items.mapIndexed { index, dto ->
            // Xác định nhãn (Badge) hiển thị dựa trên trạng thái tác phẩm
            val status = dto.status?.uppercase() ?: "AVAILABLE"
            val badge = when (status) {
                "SOLD" -> "SOLD"
                "RESERVED", "PAYMENT_SENT" -> "RESERVED"
                else -> if (index % 3 == 0) "HOT" else ""
            }
            ArtworkUiModel(
                id = dto.id,
                title = dto.title,
                author = dto.sellerName ?: "Nghệ sĩ ẩn danh",
                priceText = dto.price.stripTrailingZeros().toPlainString(),
                ratingText = listOf("4.8", "4.7", "4.9", "4.6")[index % 4],
                imageUrl = resolveImageUrl(dto.imageUrl)
                    ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800",
                badge = badge
            )
        }
    }

    /**
     * Lấy chi tiết một tác phẩm theo mã ID.
     */
    suspend fun getArtworkById(id: Long): ArtworkResponseDto {
        val response = api.getArtworkById(id)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Lỗi tải chi tiết tác phẩm")
        }
        val data = response.data ?: throw IllegalStateException("Không tìm thấy dữ liệu")
        return data.copy(imageUrl = resolveImageUrl(data.imageUrl))
    }

    /**
     * Tải ảnh lên máy chủ (Multipart).
     */
    suspend fun uploadArtworkImage(imagePart: MultipartBody.Part): String {
        val response = api.uploadArtworkImage(imagePart)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Tải ảnh lên thất bại")
        }
        val uploadData = response.data ?: throw IllegalStateException("Lỗi phản hồi tải ảnh")
        return resolveImageUrl(uploadData.imageUrl) ?: uploadData.imageUrl
    }

    /**
     * Tạo bài đăng tác phẩm mới sau khi ảnh đã được tải lên thành công.
     */
    suspend fun createArtwork(
        title: String,
        description: String,
        price: String,
        category: String,
        imageUrl: String
    ): ArtworkResponseDto {
        val response = api.createArtwork(
            ArtworkCreateRequestDto(
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl
            )
        )
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Tạo bài đăng thất bại")
        }
        return response.data ?: throw IllegalStateException("Lỗi dữ liệu khi tạo")
    }

    /**
     * Chuẩn hóa đường dẫn ảnh từ server sang URL đầy đủ.
     */
    private fun resolveImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw

        val base = BuildConfig.BASE_URL.trimEnd('/')
        val path = if (raw.startsWith('/')) raw else "/$raw"
        return "$base$path"
    }
}

/**
 * Quản lý xác thực và thông tin cá nhân của người dùng.
 */
class AuthRepository(
    private val api: AuthApi = NetworkModule.authApi
) {

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        avatarUrl: String? = null
    ): AuthResponseDto {
        val response = api.register(RegisterRequestDto(email, password, fullName, avatarUrl))
        if (!response.success) throw IllegalStateException(response.message)
        val authData = response.data ?: throw IllegalStateException("Lỗi đăng ký")
        persistSession(authData)
        return authData
    }

    suspend fun login(email: String, password: String): AuthResponseDto {
        val response = api.login(LoginRequestDto(email, password))
        if (!response.success) throw IllegalStateException(response.message)
        val authData = response.data ?: throw IllegalStateException("Lỗi đăng nhập")
        persistSession(authData)
        return authData
    }

    /**
     * Kích hoạt quyền Người bán (Seller) để cho phép đăng tranh.
     */
    suspend fun enableSellerRole(): UserProfileResponseDto {
        val response = api.enableSeller()
        if (!response.success) throw IllegalStateException(response.message)
        val profile = response.data ?: throw IllegalStateException("Không thể kích hoạt quyền người bán")
        SessionManager.userRoles = profile.roles
        return profile
    }

    private fun persistSession(authData: AuthResponseDto) {
        SessionManager.saveAuth(
            accessToken = authData.accessToken,
            tokenType = authData.tokenType,
            userId = authData.user.id,
            email = authData.user.email,
            roles = authData.user.roles
        )
    }

    fun logout() {
        SessionManager.clear()
    }
}

/**
 * Xử lý các đơn hàng và luồng thanh toán (Payment flow).
 */
class OrderRepository(
    private val api: OrderApi = NetworkModule.orderApi
) {
    suspend fun createOrder(artworkId: Long, note: String?): OrderResponseDto {
        val response = try {
            api.createOrder(OrderCreateRequestDto(artworkId, note?.trim()))
        } catch (error: HttpException) {
            throw IllegalStateException("Lỗi tạo đơn hàng: ${error.message}")
        }
        if (!response.success) throw IllegalStateException(response.message)
        return response.data ?: throw IllegalStateException("Dữ liệu đơn hàng trống")
    }

    suspend fun markPaymentSent(orderId: Long): OrderResponseDto {
        val response = try {
            api.markPaymentSent(orderId)
        } catch (error: HttpException) {
            throw IllegalStateException("Lỗi xác nhận thanh toán")
        }
        if (!response.success) throw IllegalStateException(response.message)
        return response.data ?: throw IllegalStateException("Phản hồi thanh toán trống")
    }

    suspend fun getMyOrders(): List<OrderResponseDto> {
        val response = api.getMyOrders()
        return if (response.success) response.data ?: emptyList() else emptyList()
    }

    private fun parseApiErrorMessage(error: HttpException, fallback: String): String {
        val raw = error.response()?.errorBody()?.string().orEmpty()
        return runCatching { JSONObject(raw).optString("message", fallback) }.getOrDefault(fallback)
    }
}

/**
 * Quản lý thông báo đẩy và trạng thái tin nhắn của người dùng.
 */
class NotificationRepository(
    private val api: NotificationApi = NetworkModule.notificationApi
) {
    suspend fun getMyNotifications(): List<NotificationResponseDto> {
        val response = api.getMyNotifications()
        if (!response.success) return emptyList()
        return response.data ?: emptyList()
    }

    suspend fun markAllRead(): Int {
        val response = api.markAllRead()
        if (!response.success) return 0
        return response.data?.get("updatedCount") ?: 0
    }
}
