package com.gallerymart.app.data.remote.api

import com.gallerymart.app.data.remote.dto.response.ArtworkResponse
import com.gallerymart.app.data.remote.dto.response.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtworkApi {
    @GET("api/artworks")
    suspend fun getArtworks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<ArtworkResponse>>

    @GET("api/artworks/{id}")
    suspend fun getArtworkDetail(
        @Path("id") id: Long
    ): ApiResponse<ArtworkResponse>
}
