package com.gallerymart.app.feature.artwork.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallerymart.app.core.ui.component.PrimaryButton
import com.gallerymart.app.core.ui.theme.*

@Composable
fun ArtworkDetailScreen() {
    var selectedTab by remember { mutableStateOf("TÁC PHẨM") }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image with Tabs Overlay
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                // Placeholder for Artwork Image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )

                // Top Navigation Icons
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

                // Tab Switcher Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    TabItem("TÁC PHẨM", selectedTab == "TÁC PHẨM") { selectedTab = "TÁC PHẨM" }
                    TabItem("KHÔNG GIAN", selectedTab == "KHÔNG GIAN") { selectedTab = "KHÔNG GIAN" }
                }
            }

            // Artwork Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SƠN MÀI",
                        style = Typography.labelMedium,
                        color = GrayText,
                        modifier = Modifier
                            .background(Surface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                        Text(text = " 4.9 (24)", style = Typography.labelMedium, color = StarYellow)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Phố Cổ Mùa Thu",
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Artist Mini Profile
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "HỌA SĨ", style = Typography.labelMedium, color = GrayText)
                        Text(text = "Bùi Xuân Phái", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "CÂU CHUYỆN TÁC PHẨM",
                    style = Typography.labelMedium,
                    color = GrayText,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tác phẩm là sự kết tinh của nhiều tháng ngày quan sát và trải nghiệm thực tế của họa sĩ.",
                    style = Typography.bodyLarge,
                    color = Black
                )
                
                Text(
                    text = "Đọc thêm câu chuyện",
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp).clickable { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Specs Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    SpecCard(title = "CHẤT LIỆU", value = "Sơn mài", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    SpecCard(title = "KÍCH THƯỚC", value = "70x90 cm", modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
            }
        }

        // Bottom Purchase Bar
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
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Black else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) White else GrayText,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SpecCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title, style = Typography.labelMedium, color = GrayText)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
