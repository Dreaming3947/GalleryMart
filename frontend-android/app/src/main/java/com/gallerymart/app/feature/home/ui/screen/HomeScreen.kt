package com.gallerymart.app.feature.home.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gallerymart.app.core.ui.theme.Background
import com.gallerymart.app.core.ui.theme.ChipGray
import com.gallerymart.app.core.ui.theme.TextPrimary
import com.gallerymart.app.core.ui.theme.TextSecondary

private data class CategoryItem(val title: String, val imageUrl: String)
private data class ArtworkItem(
    val title: String,
    val author: String,
    val price: String,
    val rating: String,
    val imageUrl: String,
    val badge: String
)

@Composable
fun HomeScreen() {
    val tags = listOf("#Oil painting", "#Landscape", "#Abstract", "#Modern")
    val categories = listOf(
        CategoryItem("Landscape", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400"),
        CategoryItem("Portrait", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400"),
        CategoryItem("Abstract", "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=400"),
        CategoryItem("Urban", "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=400")
    )
    val artworks = listOf(
        ArtworkItem(
            title = "Binh Minh Tren Bien",
            author = "Le The Anh",
            price = "16 dM",
            rating = "4.8",
            imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800",
            badge = "HOT"
        ),
        ArtworkItem(
            title = "Tinh Vat Hoa Hong",
            author = "Pham Binh Chuong",
            price = "12 dM",
            rating = "4.7",
            imageUrl = "https://images.unsplash.com/photo-1577083552431-6e5fd75fcf27?w=800",
            badge = ""
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp)
    ) {
        HeroHeader()
        SearchBar()
        Spacer(modifier = Modifier.height(14.dp))
        TagRow(tags = tags)
        FilterRow()
        SectionTitle(title = "Categories")
        CategoryRow(categories)
        SectionTitle(title = "Featured Artworks", action = "View all")
        FeaturedRow(artworks)
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1545239351-1141bd82e8a6?w=1200",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x32000000), Color(0xA0000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = "Discover\nUnique Artworks",
                color = Color.White,
                fontSize = 42.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Buy and sell original paintings from artists worldwide",
                color = Color(0xFFE5E7EB),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("EXPLORE GALLERY", active = true)
                PillButton("SELL ART", active = false)
            }
        }
    }
}

@Composable
private fun PillButton(text: String, active: Boolean) {
    val background = if (active) Color.White else Color(0x66FFFFFF)
    val textColor = if (active) Color(0xFF161616) else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SearchBar() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Search art, artists, styles...", color = Color(0xFF9CA3AF), fontSize = 13.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.FilterList, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ChipGray)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = tag, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FilterRow() {
    val filters = listOf("Style", "Price", "Size", "Medium")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { filter ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = filter, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (action.isNotBlank()) {
            Text(text = action, color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CategoryRow(categories: List<CategoryItem>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = category.imageUrl,
                    contentDescription = category.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = category.title, fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun FeaturedRow(artworks: List<ArtworkItem>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artworks) { artwork ->
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(10.dp)
            ) {
                Box {
                    AsyncImage(
                        model = artwork.imageUrl,
                        contentDescription = artwork.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    if (artwork.badge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF4D4F))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = artwork.badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = artwork.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = artwork.price, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "* ${artwork.rating}", color = Color(0xFFF59E0B), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "By ${artwork.author}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}


