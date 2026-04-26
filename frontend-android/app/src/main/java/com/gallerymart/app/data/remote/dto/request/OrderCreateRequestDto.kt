package com.gallerymart.app.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class OrderCreateRequestDto(
    @SerializedName("artworkId")
    val artworkId: Long,
    @SerializedName("note")
    val note: String? = null
)
