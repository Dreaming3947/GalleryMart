package com.gallerymart.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gallerymart.app.navigation.AppNavGraph

@Composable
fun GalleryMartApp() {
    AppNavGraph()
}

@Preview(showBackground = true)
@Composable
private fun GalleryMartAppPreview() {
    GalleryMartApp()
}

