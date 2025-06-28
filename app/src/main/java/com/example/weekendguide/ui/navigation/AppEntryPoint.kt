package com.example.weekendguide.ui.navigation

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.ViewModelFactory
import androidx.compose.runtime.getValue
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppEntryPoint() {

    val context = LocalContext.current.applicationContext

    // ✅ создаём LoginViewModel
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            auth = FirebaseAuth.getInstance(),
            oneTapClient = Identity.getSignInClient(context),
            userPreferences = UserPreferences(context)
        )
    )

    // ✅ создаём ThemeViewModel
    val themeViewModel: ThemeViewModel = viewModel(
        key = "ThemeViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
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
            themeViewModel = themeViewModel,
            loginViewModel = loginViewModel
        )
    }
}
