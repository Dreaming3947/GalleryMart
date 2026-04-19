package com.example.trangchu

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ProductRepository(context: Context) : ProductGateway {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getDisplayProducts(): List<Product> {
        val uploaded = getUploadedProducts()
        return if (uploaded.isNotEmpty()) uploaded else sampleProducts()
    }

    override suspend fun getProductDetail(productId: String): Product? {
        return getDisplayProducts().firstOrNull { it.id == productId }
    }

    override suspend fun createProduct(payload: UploadProductPayload): Product {
        return addUploadedProduct(
            title = payload.title,
            artist = payload.artist,
            priceVnd = payload.priceVnd,
            style = payload.style,
            tags = payload.tags,
            description = payload.description,
            material = payload.material,
            size = payload.size,
            location = payload.location,
            imageUri = payload.imageUri
        )
    }

    fun addUploadedProduct(
        title: String,
        artist: String,
        priceVnd: Long,
        style: String,
        tags: List<String>,
        description: String,
        material: String,
        size: String,
        location: String,
        imageUri: String?
    ): Product {
        val current = getUploadedProducts().toMutableList()
        val imagePool = sampleProducts().map { it.imageRes }
        val imageRes = imagePool[current.size % imagePool.size]
        val product = Product(
            id = UUID.randomUUID().toString(),
            title = title,
            artist = artist,
            priceVnd = priceVnd,
            style = style,
            tags = tags,
            imageRes = imageRes,
            imageUri = imageUri,
            description = description,
            material = material,
            size = size,
            location = location,
            ratingText = "★ 5.0"
        )
        current.add(product)
        saveUploadedProducts(current)
        return product
    }

    private fun getUploadedProducts(): List<Product> {
        val raw = prefs.getString(KEY_UPLOADED_PRODUCTS, null) ?: return emptyList()
        val jsonArray = runCatching { JSONArray(raw) }.getOrElse { return emptyList() }
        val items = mutableListOf<Product>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            items.add(
                Product(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    artist = obj.optString("artist"),
                    priceVnd = obj.optLong("priceVnd"),
                    style = obj.optString("style"),
                    tags = parseTags(obj.optJSONArray("tags")),
                    imageRes = obj.optInt("imageRes", R.drawable.artwork_1),
                    imageUri = obj.optString("imageUri").ifBlank { null },
                    description = obj.optString("description", ""),
                    material = obj.optString("material", "Sơn dầu"),
                    size = obj.optString("size", "60x80 cm"),
                    location = obj.optString("location", "Việt Nam"),
                    ratingText = obj.optString("ratingText", "★ 4.9")
                )
            )
        }
        return items.filter { it.id.isNotBlank() && it.title.isNotBlank() && it.artist.isNotBlank() }
    }

    private fun saveUploadedProducts(products: List<Product>) {
        val jsonArray = JSONArray()
        products.forEach { product ->
            val obj = JSONObject()
                .put("id", product.id)
                .put("title", product.title)
                .put("artist", product.artist)
                .put("priceVnd", product.priceVnd)
                .put("style", product.style)
                .put("imageRes", product.imageRes)
                .put("imageUri", product.imageUri ?: "")
                .put("description", product.description)
                .put("material", product.material)
                .put("size", product.size)
                .put("location", product.location)
                .put("ratingText", product.ratingText)
                .put("tags", JSONArray(product.tags))
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_UPLOADED_PRODUCTS, jsonArray.toString()).apply()
    }

    private fun parseTags(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val tags = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val raw = array.optString(i).trim()
            if (raw.isNotBlank()) tags.add(raw)
        }
        return tags
    }

    private fun sampleProducts(): List<Product> {
        return listOf(
            Product(
                id = "sample_1",
                title = "Binh Minh Trên Biển",
                artist = "Lê Thế Anh",
                priceVnd = 16_000_000,
                style = "Landscape",
                tags = listOf("landscape", "calm", "nature"),
                imageRes = R.drawable.artwork_1,
                description = "Bức tranh tái hiện bình minh trên mặt biển với tông xanh mát và cảm giác yên bình.",
                material = "Sơn dầu",
                size = "60x80 cm",
                location = "Hà Nội, Việt Nam"
            ),
            Product(
                id = "sample_2",
                title = "Tình Vắt Hoa Hồng",
                artist = "Phạm Bình Chương",
                priceVnd = 12_000_000,
                style = "Portrait",
                tags = listOf("portrait", "modern", "emotion"),
                imageRes = R.drawable.artwork_2,
                description = "Tác phẩm chân dung với gam màu hiện đại và chiều sâu cảm xúc rõ rệt.",
                material = "Acrylic",
                size = "50x70 cm",
                location = "TP. Hồ Chí Minh, Việt Nam"
            ),
            Product(
                id = "sample_3",
                title = "Mùa Vàng Trên Núi",
                artist = "Nguyễn Quốc Huy",
                priceVnd = 20_000_000,
                style = "Abstract",
                tags = listOf("abstract", "energy", "mountain"),
                imageRes = R.drawable.artwork_3,
                description = "Màu sắc mạnh và nét cọ dày gợi nên nguồn năng lượng từ thiên nhiên vùng cao.",
                material = "Sơn mài",
                size = "70x90 cm",
                location = "Đà Nẵng, Việt Nam"
            ),
            Product(
                id = "sample_4",
                title = "Mây Đen Phố Cũ",
                artist = "Trần Minh Khoa",
                priceVnd = 8_000_000,
                style = "Modern",
                tags = listOf("modern", "urban", "energy"),
                imageRes = R.drawable.artwork_4,
                description = "Nhịp sống đô thị được thể hiện qua hình khối tối giản và màu sắc đối lập.",
                material = "Mixed media",
                size = "45x60 cm",
                location = "Huế, Việt Nam"
            ),
            Product(
                id = "sample_5",
                title = "Phố Cổ Mùa Thu",
                artist = "Bùi Xuân Phái",
                priceVnd = 35_000_000,
                style = "Urban",
                tags = listOf("calm", "street", "nostalgia"),
                imageRes = R.drawable.artwork_6,
                description = "Không gian phố cũ mùa thu mang cảm giác hoài niệm và chiều sâu thời gian.",
                material = "Sơn dầu",
                size = "80x100 cm",
                location = "Hà Nội, Việt Nam"
            ),
            Product(
                id = "sample_6",
                title = "Nắng Thủy Tinh",
                artist = "Hồng Việt Dũng",
                priceVnd = 42_000_000,
                style = "Abstract",
                tags = listOf("energy", "color", "modern"),
                imageRes = R.drawable.artwork_7,
                description = "Những lớp màu chuyển động tạo nhịp điệu thị giác đầy năng lượng.",
                material = "Acrylic",
                size = "90x120 cm",
                location = "Paris, Pháp"
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "product_repo"
        private const val KEY_UPLOADED_PRODUCTS = "uploaded_products"
    }
}



