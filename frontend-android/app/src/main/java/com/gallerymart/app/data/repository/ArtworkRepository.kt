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

class ArtworkRepository(
    private val api: ArtworkApi = NetworkModule.artworkApi
) {
    /**
     * Load featured artworks for Home/Explore.
     *
     * Mapping to [ArtworkUiModel] happens here to keep UI screens focused on rendering,
     * not on transport DTO details.
     */
    suspend fun getFeaturedArtworks(): List<ArtworkUiModel> {
        val response = api.searchArtworks(page = 0, size = 12)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "API request failed")
        }
        val page = response.data ?: return emptyList()

        return page.items
            .mapIndexed { index, dto ->
            val status = dto.status?.uppercase() ?: "AVAILABLE"
            val badge = when (status) {
                "SOLD" -> "SOLD"
                "RESERVED" -> "RESERVED"
                else -> if (index % 3 == 0) "HOT" else ""
            }
            ArtworkUiModel(
                id = dto.id,
                title = dto.title,
                author = dto.sellerName ?: "Unknown artist",
                priceText = dto.price.stripTrailingZeros().toPlainString(),
                ratingText = listOf("4.8", "4.7", "4.9", "4.6")[index % 4],
                imageUrl = resolveImageUrl(dto.imageUrl)
                    ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800",
                badge = badge
            )
        }
    }

    /**
     * Fetch detail by id for ArtworkDetailActivity.
     */
    suspend fun getArtworkById(id: Long): ArtworkResponseDto {
        val response = api.getArtworkById(id)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "API request failed")
        }
        val data = response.data ?: throw IllegalStateException("Artwork not found")
        return data.copy(imageUrl = resolveImageUrl(data.imageUrl))
    }

    /**
     * Upload image file to backend.
     *
     * Why: Separate from createArtwork so upload progress can be shown independently.
     * Response includes imageUrl to use in artwork creation payload.
     */
    suspend fun uploadArtworkImage(imagePart: MultipartBody.Part): String {
        val response = api.uploadArtworkImage(imagePart)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Image upload failed")
        }
        val uploadData = response.data ?: throw IllegalStateException("Upload response missing imageUrl")
        return resolveImageUrl(uploadData.imageUrl) ?: uploadData.imageUrl
    }

    /**
     * Create artwork after image is uploaded.
     *
     * Takes the imageUrl from uploadArtworkImage() response and uses it in payload.
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
            throw IllegalStateException(response.message ?: "Create artwork failed")
        }
        return response.data ?: throw IllegalStateException("Artwork creation failed")
    }

    private fun resolveImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw

        val base = BuildConfig.BASE_URL.trimEnd('/')
        val path = if (raw.startsWith('/')) raw else "/$raw"
        return "$base$path"
    }
}

/**
 * AuthRepository handles all account/session operations.
 *
 * Why this class exists:
 * - Keep auth API details away from UI layer.
 * - Keep token persistence in one place for easy debug.
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
        val response = api.register(
            RegisterRequestDto(
                email = email,
                password = password,
                fullName = fullName,
                avatarUrl = avatarUrl
            )
        )
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Register failed")
        }
        val authData = response.data ?: throw IllegalStateException("Missing auth payload")
        persistSession(authData)
        return authData
    }

    suspend fun login(email: String, password: String): AuthResponseDto {
        val response = api.login(LoginRequestDto(email = email, password = password))
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Login failed")
        }
        val authData = response.data ?: throw IllegalStateException("Missing auth payload")
        persistSession(authData)
        return authData
    }

    suspend fun getProfile(): UserProfileResponseDto {
        val response = api.getProfile()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Load profile failed")
        }
        return response.data ?: throw IllegalStateException("Profile not found")
    }

    suspend fun updateProfile(fullName: String, avatarUrl: String?): UserProfileResponseDto {
        val response = api.updateProfile(UpdateProfileRequestDto(fullName = fullName, avatarUrl = avatarUrl))
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Update profile failed")
        }
        return response.data ?: throw IllegalStateException("Profile not found")
    }

    suspend fun enableSellerRole(): UserProfileResponseDto {
        val response = api.enableSeller()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Enable seller failed")
        }
        val profile = response.data ?: throw IllegalStateException("Profile not found")
        SessionManager.userRoles = profile.roles
        return profile
    }

    fun logout() {
        SessionManager.clear()
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
}

/**
 * Repository for order-related backend calls.
 *
 * Keeping this separate from artwork/auth repository prevents mixed responsibilities
 * and helps isolate order-flow bugs quickly.
 */
class OrderRepository(
    private val api: OrderApi = NetworkModule.orderApi
) {
    suspend fun createOrder(artworkId: Long, note: String?): OrderResponseDto {
        val response = try {
            api.createOrder(
                OrderCreateRequestDto(
                    artworkId = artworkId,
                    note = note?.trim()?.ifBlank { null }
                )
            )
        } catch (error: HttpException) {
            throw IllegalStateException(parseApiErrorMessage(error, "Create order failed"))
        }
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Create order failed")
        }
        return response.data ?: throw IllegalStateException("Order payload missing")
    }

    suspend fun markPaymentSent(orderId: Long): OrderResponseDto {
        val response = try {
            api.markPaymentSent(orderId)
        } catch (error: HttpException) {
            throw IllegalStateException(parseApiErrorMessage(error, "Mark payment sent failed"))
        }
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Mark payment sent failed")
        }
        return response.data ?: throw IllegalStateException("Order payload missing")
    }

    suspend fun confirmOrder(orderId: Long): OrderResponseDto {
        val response = try {
            api.confirmOrder(orderId)
        } catch (error: HttpException) {
            throw IllegalStateException(parseApiErrorMessage(error, "Confirm order failed"))
        }
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Confirm order failed")
        }
        return response.data ?: throw IllegalStateException("Order payload missing")
    }

    suspend fun cancelOrder(orderId: Long): OrderResponseDto {
        val response = try {
            api.cancelOrder(orderId)
        } catch (error: HttpException) {
            throw IllegalStateException(parseApiErrorMessage(error, "Cancel order failed"))
        }
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Cancel order failed")
        }
        return response.data ?: throw IllegalStateException("Order payload missing")
    }

    suspend fun getMyOrders(): List<OrderResponseDto> {
        val response = api.getMyOrders()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Load my orders failed")
        }
        return response.data ?: emptyList()
    }

    suspend fun getSalesOrders(): List<OrderResponseDto> {
        val response = api.getSalesOrders()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Load sales orders failed")
        }
        return response.data ?: emptyList()
    }

    suspend fun getOrderById(orderId: Long): OrderResponseDto {
        val response = api.getOrderById(orderId)
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Load order failed")
        }
        return response.data ?: throw IllegalStateException("Order not found")
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

/**
 * Repository for notification endpoints used by buyer/seller timeline screens.
 */
class NotificationRepository(
    private val api: NotificationApi = NetworkModule.notificationApi
) {
    suspend fun getMyNotifications(): List<NotificationResponseDto> {
        val response = api.getMyNotifications()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Load notifications failed")
        }
        return response.data ?: emptyList()
    }

    suspend fun markAllRead(): Int {
        val response = api.markAllRead()
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Mark notifications as read failed")
        }
        return response.data?.get("updatedCount") ?: 0
    }
}

