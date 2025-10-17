package com.weekendguide.app.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weekendguide.app.data.model.UserData
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.DataRepositoryImpl
import com.weekendguide.app.data.repository.UserRemoteDataSource
import com.weekendguide.app.ui.login.LoginScreen
import com.weekendguide.app.ui.main.MainScreen
import com.weekendguide.app.ui.splash.SplashScreen
import com.weekendguide.app.ui.store.StoreScreen
import com.weekendguide.app.viewmodel.LeaderboardViewModel
import com.weekendguide.app.viewmodel.LocationViewModel
import com.weekendguide.app.viewmodel.LoginViewModel
import com.weekendguide.app.viewmodel.MarkerIconViewModel
import com.weekendguide.app.viewmodel.PointsViewModel
import com.weekendguide.app.viewmodel.SplashViewModel
import com.weekendguide.app.viewmodel.SubscriptionViewModel
import com.weekendguide.app.viewmodel.ThemeViewModel
import com.weekendguide.app.viewmodel.TranslateViewModel


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
    subscriptionViewModel: SubscriptionViewModel,
    splashViewModel: SplashViewModel,
    userData: UserData
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                splashViewModel = splashViewModel,
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
                userData = userData,
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
                userData = userData,
                isInitialSelection = true,
                onRegionChosen = {
                    navController.navigate("main") {
                        popUpTo("store") { inclusive = true }
                    }
                },
                onDismiss = {/*null*/},
                userPreferences = userPreferences,
                dataRepository = dataRepository,
                userRemoteDataSource = userRemoteDataSource,
            )
        }

        composable("main") {
            MainScreen(
                app = app,
                themeViewModel = themeViewModel,
                translateViewModel = translateViewModel,
                locationViewModel = locationViewModel,
                pointsViewModel = pointsViewModel,
                userPreferences = userPreferences,
                dataRepository = dataRepository,
                userRemote = userRemoteDataSource,
                leaderboardViewModel = leaderboardViewModel,
                markerIconViewModel = markerIconViewModel,
                subscriptionViewModel = subscriptionViewModel,
                userData = userData,
                )
        }

    }
}