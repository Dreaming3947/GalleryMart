package com.example.trangchu

data class UploadProductPayload(
    val title: String,
    val artist: String,
    val priceVnd: Long,
    val style: String,
    val tags: List<String>,
    val description: String,
    val material: String,
    val size: String,
    val location: String,
    val imageUri: String?
)

/**
 * Contract de tach UI khoi data source.
 * Backend team chi can thay implementation cua interface nay.
 */
interface ProductGateway {
    suspend fun getDisplayProducts(): List<Product>
    suspend fun getProductDetail(productId: String): Product?
    suspend fun createProduct(payload: UploadProductPayload): Product
}

