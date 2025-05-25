package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

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
            delay(1500) // Показ логотипа 1.5 сек
            detectLanguage()
            decideNextScreen()
        }
    }

    private suspend fun detectLanguage() {
        val saved = preferences.getLanguage()
        if (saved == null) {
            val systemLang = Locale.getDefault().language
            val supported = listOf("en", "de", "ru")
            val selected = if (supported.contains(systemLang)) systemLang else "en"
            preferences.saveLanguage(selected)
        }
    }

    private fun decideNextScreen() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _uiState.value = Destination.Login
        } else {
            _uiState.value = Destination.RegionSelect
        }
    }
}
