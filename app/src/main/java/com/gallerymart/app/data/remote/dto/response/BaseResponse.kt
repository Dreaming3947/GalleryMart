package com.gallerymart.app.data.remote.dto.response

data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
