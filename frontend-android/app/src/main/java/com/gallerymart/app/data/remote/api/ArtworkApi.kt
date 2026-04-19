package com.gallerymart.app.data.remote.api

import com.gallerymart.app.data.remote.dto.ApiResponseDto
import com.gallerymart.app.data.remote.dto.ArtworkPageResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ArtworkApi {
    @GET("api/artworks")
    suspend fun searchArtworks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("keyword") keyword: String? = null
    ): ApiResponseDto<ArtworkPageResponseDto>
}

