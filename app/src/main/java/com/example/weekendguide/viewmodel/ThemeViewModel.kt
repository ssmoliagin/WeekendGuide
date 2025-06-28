package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)

    private val _theme = MutableStateFlow("light")
    val theme: StateFlow<String> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            _theme.value = prefs.getTheme()
        }
    }

    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            prefs.saveTheme(newTheme)
            _theme.value = newTheme
        }
    }
}
