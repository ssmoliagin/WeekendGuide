package com.example.weekendguide.ui.navigation

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.LocalesRepoImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.example.weekendguide.viewmodel.LeaderboardViewModel
import com.example.weekendguide.viewmodel.LeaderboardViewModelFactory
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LocationViewModelFactory
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.example.weekendguide.viewmodel.MarkerIconViewModel
import com.example.weekendguide.viewmodel.MarkerIconViewModelFactory
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.PointsViewModelFactory
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.SplashViewModelFactory
import com.example.weekendguide.viewmodel.SubscriptionViewModel
import com.example.weekendguide.viewmodel.SubscriptionViewModelFactory
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.ThemeViewModelFactory
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.example.weekendguide.viewmodel.TranslateViewModelFactory
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun AppEntryPoint() {

    val context = LocalContext.current.applicationContext
    val app = context as Application
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val messaging = remember { FirebaseMessaging.getInstance() }

    val userPreferences = remember { UserPreferences(context) }
    val userRemoteDataSource = remember { UserRemoteDataSource(auth, firestore, messaging, userPreferences) }

    val dataRepository = remember { DataRepositoryImpl(context) }
    val localesRepo = remember { LocalesRepoImpl(context) }

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            auth = auth,
            oneTapClient = Identity.getSignInClient(context),
            userPreferences = userPreferences,
            userRemote = userRemoteDataSource
        )
    )

    val subscriptionViewModel: SubscriptionViewModel = viewModel(
        factory = SubscriptionViewModelFactory(auth, firestore, userPreferences, userRemoteDataSource)
    )

    val translateViewModel: TranslateViewModel = viewModel(
        factory = TranslateViewModelFactory(localesRepo, userPreferences, userRemoteDataSource)
    )

    val markerIconViewModel: MarkerIconViewModel = viewModel(
        factory = MarkerIconViewModelFactory(app)
    )

    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(userPreferences, userRemoteDataSource)
    )

    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(app, userPreferences, userRemoteDataSource)
    )

    val pointsViewModel: PointsViewModel = viewModel(
        factory = PointsViewModelFactory(userPreferences, userRemoteDataSource)
    )

    val leaderboardViewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModelFactory(auth, firestore)
    )

    val splashViewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory(userPreferences)
    )

    val userData by splashViewModel.userData.collectAsState()

    //val currentTheme by themeViewModel.theme.collectAsState()
    val currentTheme = userData.userThema

    val isDarkTheme = when (currentTheme) {
        "light" -> false
        "dark" -> true
        "system" -> isSystemInDarkTheme()
        else -> false
    }

    WeekendGuideTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            app = app,
            themeViewModel = themeViewModel,
            loginViewModel = loginViewModel,
            translateViewModel = translateViewModel,
            locationViewModel = locationViewModel,
            pointsViewModel = pointsViewModel,
            userPreferences = userPreferences,
            dataRepository = dataRepository,
            userRemoteDataSource = userRemoteDataSource,
            leaderboardViewModel = leaderboardViewModel,
            markerIconViewModel = markerIconViewModel,
            subscriptionViewModel = subscriptionViewModel,
            splashViewModel = splashViewModel,
            userData = userData,
        )
    }
}
