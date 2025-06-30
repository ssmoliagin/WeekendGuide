package com.example.weekendguide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.ui.splash.SplashScreen
import com.example.weekendguide.ui.login.LoginScreen
import com.example.weekendguide.ui.region.SelectRegionScreen
import com.example.weekendguide.ui.main.MainScreen
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity


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
                    SplashViewModel.Destination.RegionSelect -> navController.navigate("region") {
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
            SelectRegionScreen(
                translateViewModel = translateViewModel,
                onRegionSelected = {
                navController.navigate("main") {
                    popUpTo("region") { inclusive = true }
                }
            })
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