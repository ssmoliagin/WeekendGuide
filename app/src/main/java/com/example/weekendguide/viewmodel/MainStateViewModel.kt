package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.BuildConfig
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainStateViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainStateViewModel::class.java)) {
            return MainStateViewModel(userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainStateViewModel(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    val userData: StateFlow<UserData> = userPreferences.userDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserData()
    )

    fun loadUserData() = viewModelScope.launch {
        val currentAppVersion = BuildConfig.VERSION_NAME
        val currentData = userPreferences.userDataFlow.first()
        val updatedData = currentData.copy(app_version = currentAppVersion)
        userPreferences.saveUserData(updatedData)
        userRemote.launchSyncLocalToRemote(viewModelScope)
    }

    fun checkSubscription() = viewModelScope.launch {
        val currentData = userPreferences.userDataFlow.first()
        val isSubscription = currentData.subscription
        val subscriptionRegions = currentData.subscriptionRegions

        if (isSubscription == false && subscriptionRegions.isNotEmpty()) {
            val updatedRegions = currentData.collectionRegions.filterNot {
                it.region_code in subscriptionRegions
            }

            val updatedData = currentData.copy(
                collectionRegions = updatedRegions,
                subscriptionRegions = emptyList()
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}