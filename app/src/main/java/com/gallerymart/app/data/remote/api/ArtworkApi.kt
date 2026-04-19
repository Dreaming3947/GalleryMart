package com.gallerymart.app.data.remote.api

import com.gallerymart.app.data.remote.dto.response.BaseResponse
import com.gallerymart.app.feature.artwork.model.Artwork
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ArtworkApi {
    @GET("api/artworks/my")
    suspend fun getMyArtworks(): Response<BaseResponse<List<Artwork>>>

    @Multipart
    @POST("api/artworks")
    suspend fun createArtwork(
        @Part("artwork") artwork: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<Artwork>>

    @Multipart
    @PUT("api/artworks/{id}")
    suspend fun updateArtwork(
        @Path("id") id: Long,
        @Part("artwork") artwork: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<Artwork>>

    @DELETE("api/artworks/{id}")
    suspend fun deleteArtwork(@Path("id") id: Long): Response<BaseResponse<Unit>>

    @POST("api/auth/me/enable-seller")
    suspend fun enableSeller(): Response<BaseResponse<Unit>>
}
