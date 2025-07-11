package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<Destination>(Destination.Loading)
    val uiState: StateFlow<Destination> = _uiState

    sealed class Destination {
        object Login : Destination()
        object Store : Destination()
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
            val regions = userPreferences.getCollectionRegions()
            _uiState.value = if (regions.isNotEmpty()) Destination.Main else Destination.Store
        }
    }
}
