package com.gallerymart.app.domain.repository

import com.gallerymart.app.core.util.Resource
import com.gallerymart.app.feature.artwork.model.Artwork

import java.io.File

interface ArtworkRepository {
    suspend fun getMyArtworks(): Resource<List<Artwork>>
    suspend fun createArtwork(artwork: Artwork, imageFile: File?): Resource<Artwork>
    suspend fun updateArtwork(id: Long, artwork: Artwork, imageFile: File?): Resource<Artwork>
    suspend fun deleteArtwork(id: Long): Resource<Unit>
    suspend fun enableSeller(): Resource<Unit>
}
