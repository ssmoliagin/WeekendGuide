package com.example.weekendguide.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainStateViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource) : AndroidViewModel(application) {

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