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

/**
 * Định nghĩa các API liên quan đến tác phẩm nghệ thuật (Artworks).
 */
interface ArtworkApi {
    /**
     * Tìm kiếm và lấy danh sách tác phẩm có phân trang.
     */
    @GET("api/artworks")
    suspend fun searchArtworks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("keyword") keyword: String? = null
    ): ApiResponseDto<ArtworkPageResponseDto>

    /**
     * Lấy thông tin chi tiết của một tác phẩm theo ID.
     */
    @GET("api/artworks/{id}")
    suspend fun getArtworkById(
        @Path("id") id: Long
    ): ApiResponseDto<ArtworkResponseDto>

    /**
     * Tải tệp hình ảnh lên server.
     */
    @Multipart
    @POST("api/artworks/upload-image")
    suspend fun uploadArtworkImage(
        @Part file: MultipartBody.Part
    ): ApiResponseDto<ArtworkImageUploadResponseDto>

    /**
     * Tạo mới một tác phẩm nghệ thuật.
     */
    @POST("api/artworks")
    suspend fun createArtwork(
        @Body request: ArtworkCreateRequestDto
    ): ApiResponseDto<ArtworkResponseDto>
}

/**
 * Định nghĩa các API liên quan đến xác thực và thông tin người dùng.
 */
interface AuthApi {
    /**
     * Đăng ký tài khoản mới.
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponseDto<AuthResponseDto>

    /**
     * Đăng nhập vào hệ thống.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponseDto<AuthResponseDto>

    /**
     * Lấy thông tin hồ sơ cá nhân của người dùng hiện tại.
     */
    @GET("api/auth/me")
    suspend fun getProfile(): ApiResponseDto<UserProfileResponseDto>

    /**
     * Cập nhật thông tin hồ sơ cá nhân.
     */
    @PATCH("api/auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ApiResponseDto<UserProfileResponseDto>

    /**
     * Kích hoạt vai trò người bán (Seller).
     */
    @POST("api/auth/me/enable-seller")
    suspend fun enableSeller(): ApiResponseDto<UserProfileResponseDto>
}

/**
 * Định nghĩa các API liên quan đến thông báo.
 */
interface NotificationApi {
    /**
     * Lấy danh sách thông báo của người dùng.
     */
    @GET("api/notifications")
    suspend fun getMyNotifications(): ApiResponseDto<List<NotificationResponseDto>>

    /**
     * Đánh dấu tất cả thông báo là đã đọc.
     */
    @PATCH("api/notifications/read-all")
    suspend fun markAllRead(): ApiResponseDto<Map<String, Int>>
}
