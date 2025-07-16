package com.example.weekendguide.ui.navigation

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.ViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.LocalesRepoImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.data.repository.WikiRepositoryImp
import com.example.weekendguide.viewmodel.LeaderboardViewModel
import com.example.weekendguide.viewmodel.LeaderboardViewModelFactory
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LocationViewModelFactory
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.example.weekendguide.viewmodel.TranslateViewModelFactory
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun AppEntryPoint() {

    val context = LocalContext.current.applicationContext
    val app = context as Application
    val auth = FirebaseAuth.getInstance()

    // ✅ инициализировать UserRepository
    val firestore = FirebaseFirestore.getInstance()
    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val userRemoteDataSource = remember {
        UserRemoteDataSource(auth, firestore, userPreferences)
    }

    // ✅ создаём репозитории
    val dataRepository = remember { DataRepositoryImpl(context.applicationContext) }
    val localesRepo = remember { LocalesRepoImpl(context.applicationContext) }
    //val wikiRepository = remember { WikiRepositoryImp(context.applicationContext) } // создадим в POIViewModelFactory

    // ✅ создаём ViewModels
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            auth = auth,
            oneTapClient = Identity.getSignInClient(context),
            userPreferences = userPreferences,
            userRemoteDataSource = userRemoteDataSource
        )
    )

    val translateViewModel: TranslateViewModel = viewModel(
        key = "TranslateViewModel",
        factory = TranslateViewModelFactory(
            app = app,
            localesRepo = localesRepo,
            userRemote = userRemoteDataSource
        )
    )

    val themeViewModel: ThemeViewModel = viewModel(
        key = "ThemeViewModel",
        factory = ViewModelFactory(context.applicationContext as Application, userPreferences, userRemoteDataSource)
    )

    val locationViewModel: LocationViewModel = viewModel(
        key = "LocationViewModel",
        factory = LocationViewModelFactory(
            app = app,
            userPreferences = userPreferences,
            userRemoteDataSource = userRemoteDataSource
        )
    )

    val pointsViewModel: PointsViewModel = viewModel(
        key = "PointsViewModel",
        factory = ViewModelFactory(context.applicationContext as Application, userPreferences, userRemoteDataSource)
    )

    val leaderboardViewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModelFactory(
            auth = auth,
            firestore = firestore
        )
    )

    val currentTheme by themeViewModel.theme.collectAsState()

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
            leaderboardViewModel = leaderboardViewModel
        )
    }
}