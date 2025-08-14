package com.example.weekendguide.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.ui.login.LoginScreen
import com.example.weekendguide.ui.main.MainScreen
import com.example.weekendguide.ui.splash.SplashScreen
import com.example.weekendguide.ui.store.StoreScreen
import com.example.weekendguide.viewmodel.LeaderboardViewModel
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.MarkerIconViewModel
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.SubscriptionViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel


@Composable
fun AppNavigation(
    app: Application,
    themeViewModel: ThemeViewModel,
    loginViewModel: LoginViewModel,
    translateViewModel: TranslateViewModel,
    locationViewModel: LocationViewModel,
    pointsViewModel: PointsViewModel,
    userPreferences: UserPreferences,
    dataRepository: DataRepositoryImpl,
    userRemoteDataSource: UserRemoteDataSource,
    leaderboardViewModel: LeaderboardViewModel,
    markerIconViewModel: MarkerIconViewModel,
    subscriptionViewModel: SubscriptionViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                userPreferences = userPreferences,
                onNavigate = { destination ->
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
            },
                translateViewModel = translateViewModel,
                )
        }

        composable("login") {
            LoginScreen(
                loginViewModel = loginViewModel,
                translateViewModel = translateViewModel,
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
                pointsViewModel = pointsViewModel,
                userPreferences = userPreferences,
                dataRepository = dataRepository,
                userRemoteDataSource = userRemoteDataSource,
            )
        }

        composable("main") {
            MainScreen(
                app = app,
                loginViewModel = loginViewModel,
                themeViewModel = themeViewModel,
                translateViewModel = translateViewModel,
                locationViewModel = locationViewModel,
                pointsViewModel = pointsViewModel,
                userPreferences = userPreferences,
                dataRepository = dataRepository,
                userRemote = userRemoteDataSource,
                leaderboardViewModel = leaderboardViewModel,
                markerIconViewModel = markerIconViewModel,
                subscriptionViewModel = subscriptionViewModel
            )
        }

    }
}