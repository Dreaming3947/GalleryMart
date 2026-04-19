package com.gallerymart.app.navigation

sealed class AppRoutes(val route: String) {
    data object Home : AppRoutes("home")
    data object Explore : AppRoutes("explore")
    data object Profile : AppRoutes("profile")
}

