package com.example.trangchu

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Backend-first gateway for Buyer Feed + Product Detail.
 * Falls back to local repository to keep app usable when API is unavailable.
 */
class BackendProductGateway(
    context: Context,
    private val baseUrl: String = DEFAULT_API_BASE_URL
) : ProductGateway {

    private val localFallback = ProductRepository(context)
    private val detailCache = ConcurrentHashMap<String, Product>()

    override suspend fun getDisplayProducts(): List<Product> = withContext(Dispatchers.IO) {
        val remoteProducts = runCatching { fetchProducts() }
            .onFailure { Log.w(TAG, "fetchProducts failed, fallback to local", it) }
            .getOrDefault(emptyList())

        if (remoteProducts.isNotEmpty()) {
            remoteProducts.forEach { detailCache[it.id] = it }
            return@withContext remoteProducts
        }

        localFallback.getDisplayProducts()
    }

    override suspend fun getProductDetail(productId: String): Product? = withContext(Dispatchers.IO) {
        detailCache[productId]?.let { return@withContext it }

        val fromRemote = runCatching { fetchProductDetail(productId) }
            .onFailure { Log.w(TAG, "fetchProductDetail failed for id=$productId", it) }
            .getOrNull()

        if (fromRemote != null) {
            detailCache[fromRemote.id] = fromRemote
            return@withContext fromRemote
        }

        localFallback.getProductDetail(productId)
    }

    override suspend fun createProduct(payload: UploadProductPayload): Product = withContext(Dispatchers.IO) {
        val created = runCatching { postProduct(payload) }
            .onFailure { Log.w(TAG, "postProduct failed, fallback to local", it) }
            .getOrNull()

        if (created != null) {
            detailCache[created.id] = created
            return@withContext created
        }

        localFallback.createProduct(payload)
    }

    private fun fetchProducts(): List<Product> {
        val response = requestJson(path = "/api/artworks?page=1&limit=50", method = "GET")
        val products = parseProductsPayload(response)
        return products.map { dto -> dto.toProduct(baseUrl) }
    }

    private fun fetchProductDetail(productId: String): Product? {
        val response = requestJson(path = "/api/artworks/$productId", method = "GET")
        return parseSingleProductPayload(response)?.toProduct(baseUrl)
    }

    private fun postProduct(payload: UploadProductPayload): Product? {
        val body = JSONObject()
            .put("title", payload.title)
            .put("artistName", payload.artist)
            .put("priceVnd", payload.priceVnd)
            .put("style", payload.style)
            .put("tags", JSONArray(payload.tags))
            .put("description", payload.description)
            .put("medium", payload.material)
            .put("size", payload.size)
            .put("location", payload.location)
            .put("imageUrl", payload.imageUri ?: "")

        val response = requestJson(path = "/api/artworks", method = "POST", body = body.toString())
        return parseSingleProductPayload(response)?.toProduct(baseUrl)
    }

    private fun requestJson(path: String, method: String, body: String? = null): JSONObject {
        val normalizedBase = baseUrl.trimEnd('/')
        val connection = (URL("$normalizedBase$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            if (body != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body)
                }
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val payload = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code for $path: $payload")
            }
            return if (payload.isBlank()) JSONObject() else JSONObject(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProductsPayload(root: JSONObject): List<ArtworkDto> {
        val directArray = root.optJSONArray("data")
            ?: root.optJSONArray("items")
            ?: root.optJSONArray("artworks")

        if (directArray != null) {
            return parseArtworkArray(directArray)
        }

        // Some APIs wrap list in an object: { data: { items: [...] } }
        val dataObject = root.optJSONObject("data")
        val nestedArray = dataObject?.optJSONArray("items")
            ?: dataObject?.optJSONArray("artworks")

        return parseArtworkArray(nestedArray)
    }

    private fun parseSingleProductPayload(root: JSONObject): ArtworkDto? {
        root.optJSONObject("data")?.let { return ArtworkDto.fromJson(it) }
        root.optJSONObject("artwork")?.let { return ArtworkDto.fromJson(it) }

        // If response itself is the artwork object.
        return ArtworkDto.fromJson(root)
    }

    private fun parseArtworkArray(array: JSONArray?): List<ArtworkDto> {
        if (array == null) return emptyList()
        val out = mutableListOf<ArtworkDto>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            ArtworkDto.fromJson(item)?.let(out::add)
        }
        return out
    }

    private data class ArtworkDto(
        val id: String,
        val title: String,
        val artistName: String,
        val priceVnd: Long,
        val style: String,
        val tags: List<String>,
        val imagePath: String?,
        val description: String,
        val medium: String,
        val size: String,
        val location: String,
        val rating: Double
    ) {
        fun toProduct(baseUrl: String): Product {
            val resolvedImage = resolveImageUrl(baseUrl, imagePath)
            return Product(
                id = id,
                title = title,
                artist = artistName,
                priceVnd = priceVnd,
                style = style,
                tags = tags,
                imageRes = R.drawable.artwork_1,
                imageUri = resolvedImage,
                description = description,
                material = medium,
                size = size,
                location = location,
                ratingText = "★ ${"%.1f".format(rating.coerceAtLeast(0.0))}"
            )
        }

        companion object {
            fun fromJson(json: JSONObject): ArtworkDto? {
                val id = json.optString("id")
                    .ifBlank { json.optString("artworkId") }
                    .ifBlank { return null }

                val title = json.optString("title").ifBlank { return null }
                val artistName = json.optString("artistName")
                    .ifBlank { json.optString("artist") }
                    .ifBlank { "Unknown" }

                val price = when {
                    json.has("priceVnd") -> json.optLong("priceVnd")
                    json.has("price") -> json.optLong("price")
                    else -> 0L
                }

                val style = json.optString("style").ifBlank { "Unknown" }
                val tags = parseTags(json)
                val imagePath = json.optString("imageUrl")
                    .ifBlank { json.optString("imagePath") }
                    .ifBlank { null }
                val description = json.optString("description", "")
                val medium = json.optString("medium")
                    .ifBlank { json.optString("material") }
                    .ifBlank { "Unknown" }
                val size = json.optString("size").ifBlank { "Unknown" }
                val location = json.optString("location").ifBlank { "Unknown" }
                val rating = json.optDouble("rating", 0.0)

                return ArtworkDto(
                    id = id,
                    title = title,
                    artistName = artistName,
                    priceVnd = price,
                    style = style,
                    tags = tags,
                    imagePath = imagePath,
                    description = description,
                    medium = medium,
                    size = size,
                    location = location,
                    rating = rating
                )
            }

            private fun parseTags(json: JSONObject): List<String> {
                val explicit = mutableListOf<String>()
                val tagsArray = json.optJSONArray("tags")
                if (tagsArray != null) {
                    for (index in 0 until tagsArray.length()) {
                        val tag = tagsArray.optString(index).trim()
                        if (tag.isNotBlank()) explicit += tag
                    }
                }
                if (explicit.isNotEmpty()) return explicit

                // Fallback: infer one tag from style so feed filters still work.
                val style = json.optString("style").trim()
                return if (style.isBlank()) emptyList() else listOf(style)
            }
        }
    }

    companion object {
        private const val TAG = "BackendProductGateway"
        private const val TIMEOUT_MS = 10_000
        private const val DEFAULT_API_BASE_URL = "http://10.0.2.2:8080"

        private fun resolveImageUrl(baseUrl: String, raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            if (raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("content://")) {
                return raw
            }

            val normalizedBase = baseUrl.trimEnd('/')
            val normalizedPath = raw.trimStart('/')

            return if (raw.startsWith("app/sampledata/")) {
                val imageName = raw.removePrefix("app/sampledata/")
                "$normalizedBase/static/sampledata/$imageName"
            } else {
                "$normalizedBase/$normalizedPath"
            }
        }
    }
}


