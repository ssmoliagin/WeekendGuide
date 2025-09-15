package com.example.weekendguide.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weekendguide.R
import com.example.weekendguide.viewmodel.SplashViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel


@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel,
    onNavigate: (SplashViewModel.Destination) -> Unit,
    translateViewModel: TranslateViewModel,
) {

    val state by splashViewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state != SplashViewModel.Destination.Loading) {
            translateViewModel.detectLanguage()
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
