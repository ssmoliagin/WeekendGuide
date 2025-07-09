package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.LocalesRepo
import com.example.weekendguide.data.repository.UserRemoteDataSource

class StatisticsViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(userPreferences, userRemoteDataSource) as T
    }
}
