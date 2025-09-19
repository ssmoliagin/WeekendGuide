package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.first
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

    fun refreshUserData() = viewModelScope.launch {
        val currentData = userPreferences.userDataFlow.first()

        val isSubscription = currentData.subscription
        val subscriptionRegions = currentData.subscriptionRegions
        val updatedRegions = currentData.collectionRegions.filterNot {
                it.region_code in subscriptionRegions }

        val updatedData = currentData.copy(
            currentCity = currentData.homeCity,
            currentLat = currentData.homeLat,
            currentLng = currentData.homeLng,
            collectionRegions = if (isSubscription == false && subscriptionRegions.isNotEmpty()) updatedRegions
            else currentData.collectionRegions,
            subscriptionRegions = if (isSubscription == false && subscriptionRegions.isNotEmpty()) emptyList()
            else currentData.subscriptionRegions
        )
        userPreferences.saveUserData(updatedData)
        userRemote.launchSyncLocalToRemote(viewModelScope)
    }
}