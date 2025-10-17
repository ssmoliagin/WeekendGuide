package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StatisticsViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(userPreferences, userRemoteDataSource) as T
    }
}

class StatisticsViewModel(
    private val userPreferences: UserPreferences,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModel() {

    fun updateCategoryLevel(category: String, level: Int) {
        viewModelScope.launch {
            userPreferences.updateCategoryLevel(category, level)
            userPreferences.updateRewardAvailable(category, reward = true)
            userRemoteDataSource.launchSyncLocalToRemote(this)

            //addGP
            val prize = 1000
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(
                current_GP = currentData.current_GP + prize,
                total_GP = currentData.total_GP + prize
            )

            userPreferences.saveUserData(updatedData)
            userRemoteDataSource.launchSyncLocalToRemote(viewModelScope)
        }

    }
    fun updateRewardAvailable(category: String) {
        viewModelScope.launch {
            userPreferences.updateRewardAvailable(category, reward = false)
            userRemoteDataSource.launchSyncLocalToRemote(this)
        }
    }
}
