package com.gallerymart.app.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class OrderResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalPrice")
    val totalPrice: BigDecimal,
    @SerializedName("note")
    val note: String?,
    @SerializedName("paymentSentAt")
    val paymentSentAt: String?,
    @SerializedName("expiresAt")
    val expiresAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("buyerId")
    val buyerId: Long,
    @SerializedName("buyerName")
    val buyerName: String?,
    @SerializedName("sellerId")
    val sellerId: Long,
    @SerializedName("sellerName")
    val sellerName: String?,
    @SerializedName("artworkId")
    val artworkId: Long,
    @SerializedName("artworkTitle")
    val artworkTitle: String?,
    @SerializedName("artworkImageUrl")
    val artworkImageUrl: String?
)
