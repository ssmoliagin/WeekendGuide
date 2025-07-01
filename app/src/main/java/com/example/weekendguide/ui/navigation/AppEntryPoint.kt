package com.example.weekendguide.ui.navigation

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weekendguide.R
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.LocalesRepoImpl
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.example.weekendguide.viewmodel.TranslateViewModelFactory
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppEntryPoint() {

    val context = LocalContext.current.applicationContext
    val app = context as Application

    // ✅ создаём репозиторий
    val localesRepo = remember { LocalesRepoImpl(context) }
    val dataRepo = remember { DataRepositoryImpl(context) }

    // ✅ создаём ViewModels
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            auth = FirebaseAuth.getInstance(),
            oneTapClient = Identity.getSignInClient(context),
            userPreferences = UserPreferences(context)
        )
    )

    val translateViewModel: TranslateViewModel = viewModel(
        key = "TranslateViewModel",
        factory = TranslateViewModelFactory(
            app = app,
            localesRepo = localesRepo,
            dataRepo = dataRepo
        )
    )

    val themeViewModel: ThemeViewModel = viewModel(
        key = "ThemeViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    val locationViewModel: LocationViewModel = viewModel(
        key = "LocationViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    val pointsViewModel: PointsViewModel = viewModel(
        key = "PointsViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    //
    val didCheckLang = remember { mutableStateOf(false) }

    if (!didCheckLang.value) {
        LaunchedEffect(Unit) {
            translateViewModel.detectLanguage()
            didCheckLang.value = true
        }
    }

    //
    val currentTheme by themeViewModel.theme.collectAsState()

    val isDarkTheme = when (currentTheme) {
        "light" -> false
        "dark" -> true
        "system" -> isSystemInDarkTheme()
        else -> false
    }

    WeekendGuideTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            themeViewModel = themeViewModel,
            loginViewModel = loginViewModel,
            translateViewModel = translateViewModel,
            locationViewModel = locationViewModel,
            pointsViewModel = pointsViewModel

        )
    }
}