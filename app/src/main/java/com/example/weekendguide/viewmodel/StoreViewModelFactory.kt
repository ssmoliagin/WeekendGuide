package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource

class StoreViewModelFactory(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val dataRepository: DataRepositoryImpl,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(application, userPreferences, userRemote, dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}