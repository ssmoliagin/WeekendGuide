package com.example.weekendguide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.weekendguide.ui.splash.SplashScreen
import com.example.weekendguide.ui.login.LoginScreen
import com.example.weekendguide.ui.region.SelectRegionScreen
import com.example.weekendguide.ui.main.MainScreen
import com.example.weekendguide.viewmodel.SplashViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigate = { destination ->
                when (destination) {
                    SplashViewModel.Destination.Login -> navController.navigate("login")
                    SplashViewModel.Destination.RegionSelect -> navController.navigate("region")
                    SplashViewModel.Destination.Main -> navController.navigate("main")
                    else -> { /* ничего не делать или логировать */ }
                }
            })
        }
        composable("login") {
            LoginScreen(onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable("region") {
            SelectRegionScreen(onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable("main") {
            MainScreen()
        }
    }
}
