package com.example.weekendguide.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainStateViewModel(private val context: Context) : ViewModel() {
    private val prefs = UserPreferences(context)

    private val _regions = MutableStateFlow<List<Region>>(emptyList())
    val regions: StateFlow<List<Region>> = _regions

    init {
        viewModelScope.launch {
            loadRegions()
        }
    }

    suspend fun loadRegions() {
        _regions.value = prefs.getHomeRegions()
    }

    fun refreshRegions() {
        viewModelScope.launch {
            loadRegions()
        }
    }
}