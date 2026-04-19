package com.gallerymart.app.data.repository

import com.gallerymart.app.core.util.Resource
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.domain.repository.ArtworkRepository
import com.gallerymart.app.feature.artwork.model.Artwork
import java.io.IOException

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ArtworkRepositoryImpl(
    private val api: ArtworkApi
) : ArtworkRepository {

    override suspend fun getMyArtworks(): Resource<List<Artwork>> {
        return try {
            val response = api.getMyArtworks()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Unknown Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network Error")
        }
    }

    override suspend fun createArtwork(artwork: Artwork, imageFile: File?): Resource<Artwork> {
        return try {
            val artworkJson = Gson().toJson(artwork)
            val artworkPart = artworkJson.toRequestBody("application/json".toMediaTypeOrNull())
            
            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response = api.createArtwork(artworkPart, imagePart)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.message() ?: "Unknown Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network Error")
        }
    }

    override suspend fun updateArtwork(id: Long, artwork: Artwork, imageFile: File?): Resource<Artwork> {
        return try {
            val artworkJson = Gson().toJson(artwork)
            val artworkPart = artworkJson.toRequestBody("application/json".toMediaTypeOrNull())
            
            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response = api.updateArtwork(id, artworkPart, imagePart)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.message() ?: "Unknown Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network Error")
        }
    }

    override suspend fun deleteArtwork(id: Long): Resource<Unit> {
        return try {
            val response = api.deleteArtwork(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Unknown Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network Error")
        }
    }

    override suspend fun enableSeller(): Resource<Unit> {
        return try {
            val response = api.enableSeller()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Unknown Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network Error")
        }
    }
}
