package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.LocalesRepo
import com.example.weekendguide.data.repository.UserRemoteDataSource

class ViewModelFactory(
    private val app: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(app, userPreferences, userRemote) as T
            }
            modelClass.isAssignableFrom(ThemeViewModel::class.java) -> {
                ThemeViewModel(app, userPreferences, userRemote) as T
            }
            modelClass.isAssignableFrom(MainStateViewModel::class.java) -> {
                MainStateViewModel(app, userPreferences, userRemote) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(app, userPreferences, userRemote) as T
            }
            modelClass.isAssignableFrom(PointsViewModel::class.java) -> {
                PointsViewModel(app, userPreferences, userRemote) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}