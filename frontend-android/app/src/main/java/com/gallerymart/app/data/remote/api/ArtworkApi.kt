package com.gallerymart.app.data.remote.api

import com.gallerymart.app.data.remote.dto.ApiResponseDto
import com.gallerymart.app.data.remote.dto.ArtworkPageResponseDto
import com.gallerymart.app.data.remote.dto.ArtworkResponseDto
import com.gallerymart.app.data.remote.dto.ArtworkCreateRequestDto
import com.gallerymart.app.data.remote.dto.ArtworkImageUploadResponseDto
import com.gallerymart.app.data.remote.dto.AuthResponseDto
import com.gallerymart.app.data.remote.dto.LoginRequestDto
import com.gallerymart.app.data.remote.dto.NotificationResponseDto
import com.gallerymart.app.data.remote.dto.RegisterRequestDto
import com.gallerymart.app.data.remote.dto.UpdateProfileRequestDto
import com.gallerymart.app.data.remote.dto.UserProfileResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtworkApi {
    @GET("api/artworks")
    suspend fun searchArtworks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("keyword") keyword: String? = null
    ): ApiResponseDto<ArtworkPageResponseDto>

    @GET("api/artworks/{id}")
    suspend fun getArtworkById(
        @Path("id") id: Long
    ): ApiResponseDto<ArtworkResponseDto>

    @Multipart
    @POST("api/artworks/upload-image")
    suspend fun uploadArtworkImage(
        @Part file: MultipartBody.Part
    ): ApiResponseDto<ArtworkImageUploadResponseDto>

    @POST("api/artworks")
    suspend fun createArtwork(
        @Body request: ArtworkCreateRequestDto
    ): ApiResponseDto<ArtworkResponseDto>
}

/**
 * Auth endpoints grouped in same API file due workspace write restrictions for new files.
 */
interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponseDto<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponseDto<AuthResponseDto>

    @GET("api/auth/me")
    suspend fun getProfile(): ApiResponseDto<UserProfileResponseDto>

    @PATCH("api/auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ApiResponseDto<UserProfileResponseDto>

    @POST("api/auth/me/enable-seller")
    suspend fun enableSeller(): ApiResponseDto<UserProfileResponseDto>
}

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getMyNotifications(): ApiResponseDto<List<NotificationResponseDto>>

    @PATCH("api/notifications/read-all")
    suspend fun markAllRead(): ApiResponseDto<Map<String, Int>>
}

