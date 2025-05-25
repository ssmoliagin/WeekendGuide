package com.example.weekendguide.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.R
import com.example.weekendguide.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onNavigate: (SplashViewModel.Destination) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state != SplashViewModel.Destination.Loading) {
            onNavigate(state)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.logo), // замените на ваш логотип
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
