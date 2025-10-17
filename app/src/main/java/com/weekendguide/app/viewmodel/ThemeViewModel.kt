package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ThemeViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ThemeViewModel(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(userThema = newTheme)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}
