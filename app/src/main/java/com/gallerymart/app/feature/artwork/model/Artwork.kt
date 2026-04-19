package com.gallerymart.app.feature.artwork.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artwork(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("material")
    val material: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("status")
    val status: String? = null // AVAILABLE, RESERVED, SOLD
) : Parcelable
