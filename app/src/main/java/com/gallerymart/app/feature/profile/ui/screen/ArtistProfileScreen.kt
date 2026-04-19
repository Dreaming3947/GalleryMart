package com.gallerymart.app.feature.profile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallerymart.app.core.ui.component.PrimaryButton
import com.gallerymart.app.core.ui.theme.*

@Composable
fun ArtistProfileScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.background(White, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Row {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(White, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(White, CircleShape)
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                }
            }

            // Artist Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(White, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Bùi Xuân Phái", style = Typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "Hà Nội, Việt Nam", style = Typography.bodyMedium, color = GrayText)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tốt nghiệp Đại học Mỹ thuật Việt Nam, nghệ sĩ nổi tiếng với phong cách đương đại pha lẫn nét truyền thống.",
                    style = Typography.bodyMedium,
                    color = GrayText,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Text("Hồ sơ", color = Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Theo dõi", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Related Artworks Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Có thể bạn thích", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GrayText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(260.dp)
            ) {
                items(listOf("Bình Minh Trên Biển", "Vũ Điệu Ánh Sáng")) { title ->
                    RelatedArtworkItem(title)
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Bottom Purchase Bar (Sticky)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 8.dp,
            color = White
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "TỔNG THANH TOÁN", style = Typography.labelMedium, color = GrayText)
                    Text(text = "35.000.000 đ", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                PrimaryButton(
                    text = "SỞ HỮU NGAY",
                    onClick = { },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Composable
fun RelatedArtworkItem(title: String) {
    Column(modifier = Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = Typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = "16 đTr", style = Typography.labelMedium, color = GrayText)
    }
}
