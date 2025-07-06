package com.example.weekendguide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weekendguide.ui.splash.SplashScreen
import com.example.weekendguide.ui.login.LoginScreen
import com.example.weekendguide.ui.main.MainScreen
import com.example.weekendguide.ui.store.StoreScreen
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel


@Composable
fun AppNavigation(
    themeViewModel: ThemeViewModel,
    loginViewModel: LoginViewModel,
    translateViewModel: TranslateViewModel,
    locationViewModel: LocationViewModel,
    pointsViewModel: PointsViewModel,
) {
    val navController = rememberNavController()



    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val splashViewModel: SplashViewModel = viewModel()
            SplashScreen(viewModel = splashViewModel, onNavigate = { destination ->
                when (destination) {
                    SplashViewModel.Destination.Login -> navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.Store -> navController.navigate("store") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.Main -> navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                    SplashViewModel.Destination.Loading -> { /* No-op */ }
                }
            })
        }

        composable("login") {
            LoginScreen(
                loginViewModel = loginViewModel,
                onNavigate = { destination ->
                when (destination) {
                    SplashViewModel.Destination.Store -> navController.navigate("store") {
                        popUpTo("login") { inclusive = true }
                    }
                    SplashViewModel.Destination.Main -> navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                    else -> {}
                }
            })
        }

        composable("store") {
            StoreScreen(
                isInitialSelection = true,
                onRegionChosen = {
                    navController.navigate("main") {
                        popUpTo("store") { inclusive = true }
                    }
                },
                translateViewModel = translateViewModel,
                pointsViewModel = pointsViewModel
            )
        }

        composable("main") {
            MainScreen(
                onLoggedOut = {
                navController.navigate("splash") {
                    popUpTo(0) { inclusive = true } // Удаляет всю навигационную историю
                }
            },
                loginViewModel = loginViewModel,
                themeViewModel = themeViewModel,
                translateViewModel = translateViewModel,
                locationViewModel = locationViewModel,
                pointsViewModel = pointsViewModel
            )
        }

    }
}