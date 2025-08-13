package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainStateViewModelFactory(
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainStateViewModel::class.java)) {
            return MainStateViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainStateViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _regions = MutableStateFlow<List<Region>>(emptyList())
    val regions: StateFlow<List<Region>> = _regions

    init {
        viewModelScope.launch {
            loadRegions()
        }
    }

    suspend fun loadRegions() {
        _regions.value = userPreferences.getCollectionRegions()
    }

    fun refreshRegions() {
        viewModelScope.launch {
            loadRegions()
        }
    }
}