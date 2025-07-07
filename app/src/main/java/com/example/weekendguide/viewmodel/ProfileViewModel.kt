package com.example.weekendguide.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context) : ViewModel() {
    private val prefs = UserPreferences(context)

    private val _units = MutableStateFlow("km")
    val units: StateFlow<String> = _units.asStateFlow()

    private val _notification = MutableStateFlow(true)
    val notification: StateFlow<Boolean> = _notification.asStateFlow()

    init {
        viewModelScope.launch {
            _units.value = prefs.getMeasurement()
            _notification.value = prefs.getNotification()
        }
    }

    fun setUserMeasurement(newMeas: String) {
        viewModelScope.launch {
            prefs.saveMeasurement(newMeas)
            _units.value = newMeas
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setNotification(enabled)
            _notification.value = enabled
        }
    }
}