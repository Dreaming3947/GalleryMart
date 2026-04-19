package com.gallerymart.app.feature.explore.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ExploreScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Explore", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(text = "Task tiep theo: hoan thien trang Kham pha", color = Color(0xFF6B7280))
    }
}

