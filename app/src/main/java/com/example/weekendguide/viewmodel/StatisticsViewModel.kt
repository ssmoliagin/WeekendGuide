package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
}