package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _categoryLevels = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categoryLevels: StateFlow<Map<String, Int>> = _categoryLevels

    private val _purchasedRegionsCount = MutableStateFlow(0)
    val purchasedRegionsCount: StateFlow<Int> = _purchasedRegionsCount

    private val _purchasedCountriesCount = MutableStateFlow(0)
    val purchasedCountriesCount: StateFlow<Int> = _purchasedCountriesCount

    init {
        viewModelScope.launch {
            userPreferences.userDataFlow.collect { userData ->
                _purchasedRegionsCount.value = userData.purchasedRegions.size
                _purchasedCountriesCount.value = userData.purchasedCountries.size
                _categoryLevels.value = userData.categoryLevels
            }
        }
    }

    fun updateCategoryLevel(category: String, level: Int) {
        viewModelScope.launch {
            userPreferences.levelUpCategory(category, level)

            // Update local state
            val updatedMap = _categoryLevels.value.toMutableMap().apply {
                put(category, level)
            }
            _categoryLevels.value = updatedMap

            // Save updated data for sync
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(categoryLevels = updatedMap)
            userPreferences.saveUserData(updatedData)

            // Sync with Firestore
            userRemoteDataSource.launchSyncLocalToRemote(viewModelScope)
        }
    }
}
