package com.gallerymart.app.data.remote.dto

data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

// =============================
// Auth request/response payloads
// =============================
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val roles: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UpdateProfileRequestDto(
    val fullName: String,
    val avatarUrl: String? = null
)

data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserProfileResponseDto
)

data class UserProfileResponseDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val roles: String
)

// =============================
// Order payloads
// =============================
data class OrderCreateRequestDto(
    val artworkId: Long,
    val note: String? = null
)

data class OrderResponseDto(
    val id: Long,
    val status: String,
    val totalPrice: String,
    val note: String?,
    val paymentSentAt: String?,
    val expiresAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val buyerId: Long,
    val buyerName: String,
    val sellerId: Long,
    val sellerName: String,
    val artworkId: Long,
    val artworkTitle: String,
    val artworkImageUrl: String?
)

// =============================
// Notification payloads
// =============================
data class NotificationResponseDto(
    val id: Long,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// =============================
// Artwork upload/create payloads
// =============================
data class ArtworkImageUploadResponseDto(
    val imageUrl: String
)

data class ArtworkCreateRequestDto(
    val title: String,
    val description: String,
    val price: String,
    val category: String,
    val imageUrl: String
)

