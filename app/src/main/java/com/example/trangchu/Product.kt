package com.example.trangchu

data class Product(
    val id: String,
    val title: String,
    val artist: String,
    val priceVnd: Long,
    val style: String,
    val tags: List<String>,
    val imageRes: Int,
    val imageUri: String? = null,
    val description: String,
    val material: String,
    val size: String,
    val location: String,
    val ratingText: String = "★ 4.9"
) {
    fun priceLabel(): String = formatVnd(priceVnd)
    fun priceCompactLabel(): String = formatCompactVnd(priceVnd)

    fun searchIndex(): String {
        return buildString {
            append(title)
            append(' ')
            append(artist)
            append(' ')
            append(style)
            append(' ')
            append(tags.joinToString(" "))
            append(' ')
            append(description)
            append(' ')
            append(material)
        }
    }

    companion object {
        fun formatVnd(price: Long): String {
            val value = price.coerceAtLeast(0)
            return "%,d VND".format(value).replace(',', '.')
        }

        fun formatCompactVnd(price: Long): String {
            val value = price.coerceAtLeast(0)
            if (value >= 1_000_000_000L) {
                val billion = value / 1_000_000_000L
                return "$billion T"
            }
            if (value >= 1_000_000L) {
                val million = value / 1_000_000L
                return "$million Tr"
            }
            if (value >= 1_000L) {
                val thousand = value / 1_000L
                return "$thousand N"
            }
            return "$value VND"
        }
    }
}


