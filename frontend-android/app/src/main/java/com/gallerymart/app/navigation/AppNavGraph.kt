package com.gallerymart.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gallerymart.app.core.ui.component.BottomNavBar
import com.gallerymart.app.feature.explore.ui.screen.ExploreScreen
import com.gallerymart.app.feature.home.ui.screen.HomeScreen
import com.gallerymart.app.feature.profile.ui.screen.ProfileScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = currentRoute, onTabSelected = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoutes.Home.route) {
                HomeScreen()
            }
            composable(AppRoutes.Explore.route) {
                ExploreScreen()
            }
            composable(AppRoutes.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

