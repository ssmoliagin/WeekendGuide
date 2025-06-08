package com.example.weekendguide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weekendguide.ui.splash.SplashScreen
import com.example.weekendguide.ui.login.LoginScreen
import com.example.weekendguide.ui.region.SelectRegionScreen
import com.example.weekendguide.ui.main.MainScreen
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.POIViewModel


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val splashViewModel: SplashViewModel = viewModel()
            SplashScreen(viewModel = splashViewModel, onNavigate = { destination ->
                when (destination) {
                    SplashViewModel.Destination.Login -> navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.RegionSelect -> navController.navigate("region") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.Main -> navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.Map -> navController.navigate("map")
                    SplashViewModel.Destination.Loading -> { /* No-op */ }
                }
            })
        }

        composable("login") {
            LoginScreen(onNavigate = { destination ->
                when (destination) {
                    SplashViewModel.Destination.RegionSelect -> navController.navigate("region") {
                        popUpTo("login") { inclusive = true }
                    }
                    SplashViewModel.Destination.Main -> navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                    else -> {}
                }
            })
        }

        composable("region") {
            SelectRegionScreen(onRegionSelected = {
                navController.navigate("main") {
                    popUpTo("region") { inclusive = true }
                }
            })
        }

        composable("main") {
            MainScreen()
        }

    }
}