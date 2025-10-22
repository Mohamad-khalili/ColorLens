package com.compose.colorlens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.compose.colorlens.utils.Route
import com.compose.colorlens.screens.CameraScreen
import com.compose.colorlens.screens.HomeScreen

@Composable
fun MyAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.HOME_SCREEN){
        composable(route = Route.HOME_SCREEN){
            HomeScreen(navController=navController)
        }

        composable(route = Route.CAMERA_SCREEN){
            CameraScreen()
        }
    }
}