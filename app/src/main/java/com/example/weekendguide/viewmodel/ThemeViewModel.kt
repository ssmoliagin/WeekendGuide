package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ThemeViewModel(
    application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(application) {

    private val _theme = MutableStateFlow("light")
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun loadTheme() {
        viewModelScope.launch {
            _theme.value = userPreferences.getTheme()
        }
    }

    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            userPreferences.saveTheme(newTheme)
            _theme.value = newTheme

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(userThema = newTheme)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}
