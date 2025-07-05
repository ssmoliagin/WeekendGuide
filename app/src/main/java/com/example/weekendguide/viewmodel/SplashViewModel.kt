package com.example.weekendguide.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(app: Application) : AndroidViewModel(app) {

    private val preferences = UserPreferences(app)
    private val _uiState = MutableStateFlow<Destination>(Destination.Loading)
    val uiState: StateFlow<Destination> = _uiState

    sealed class Destination {
        object Login : Destination()
        object RegionSelect : Destination()
        object Main : Destination()
        object Loading : Destination()
    }

    init {
        viewModelScope.launch {
            delay(1500)
            decideNextScreen()
        }
    }

    private suspend fun decideNextScreen() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _uiState.value = Destination.Login
        } else {
            val regions = preferences.getHomeRegions()
            _uiState.value = if (regions.isNotEmpty()) Destination.Main else Destination.RegionSelect

            Log.d("DEBUG", "regions count: ${regions.size}")
        }
    }
}
