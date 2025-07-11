package com.example.weekendguide.ui.splash

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.R
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.viewmodel.MainStateViewModel
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.ViewModelFactory


@Composable
fun SplashScreen(
    app: Application,
    onNavigate: (SplashViewModel.Destination) -> Unit,
    userPreferences: UserPreferences,
    userRemote: UserRemoteDataSource
) {
    val viewModel: SplashViewModel = viewModel(
        key = "SplashViewModel",
        factory = ViewModelFactory(app, userPreferences, userRemote)
    )

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state != SplashViewModel.Destination.Loading) {
            onNavigate(state)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
