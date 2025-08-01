package com.example.weekendguide.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class PointsViewModel(
    application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(application) {

    private val _currentGP = MutableStateFlow(0)
    private val _totalGP = MutableStateFlow(0)
    private val _spentGP = MutableStateFlow(0)
    val currentGP: StateFlow<Int> = _currentGP.asStateFlow()
    val totalGP: StateFlow<Int> = _totalGP.asStateFlow()
    val spentGP: StateFlow<Int> = _spentGP.asStateFlow()

    private val _premium = MutableStateFlow(true)
    val premium: StateFlow<Boolean> = _premium.asStateFlow()

    fun refreshGP() = viewModelScope.launch {
        _currentGP.value = userPreferences.getCurrentGP()
        _totalGP.value = userPreferences.getTotalGP()
        _spentGP.value = userPreferences.getSpentGP()
        _premium.value = userPreferences.getPremium()
    }

    fun addGP(amount: Int) = viewModelScope.launch {
        val (current, total) = userPreferences.addGPAndGet(amount)

        _currentGP.value = current
        _totalGP.value = total

        val currentData = userPreferences.userDataFlow.first()
        val updatedData = currentData.copy(
            current_GP = current,
            total_GP = total
        )
        userPreferences.saveUserData(updatedData)
        userRemote.launchSyncLocalToRemote(viewModelScope)
    }

    fun spentGP(amount: Int) = viewModelScope.launch {
        val success = userPreferences.spentGP(amount)
        if (success) {
            _currentGP.value = userPreferences.getCurrentGP()
            _spentGP.value = userPreferences.getSpentGP()

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(
                current_GP = _currentGP.value,
                spent_GP = _spentGP.value
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun resetGP() = viewModelScope.launch {
        userPreferences.resetGP()
        _currentGP.value = 0
        _totalGP.value = 0
        _spentGP.value = 0

        val currentData = userPreferences.userDataFlow.first()
        val updatedData = currentData.copy(
            current_GP = 0,
            total_GP = 0,
            spent_GP = 0
        )
        userPreferences.saveUserData(updatedData)
        userRemote.launchSyncLocalToRemote(viewModelScope)
    }

    suspend fun checkAndAwardGPForPOI(
        poi: POI,
        locationViewModel: LocationViewModel,
        onResult: (Boolean) -> Unit
    ) {
        val minDuration = 2000L
        val maxTimeout = 6000L
        val startTime = System.currentTimeMillis()

        try {
            val newLocation = withTimeoutOrNull(maxTimeout) {
                locationViewModel.detectLocationFromGPS()
            }

            if (newLocation == null) {
                onResult(false)
                return
            }

            val result = FloatArray(1)
            Location.distanceBetween(
                newLocation.first,
                newLocation.second,
                poi.lat,
                poi.lng,
                result
            )
            val distanceMeters = result[0]
            val success = distanceMeters < 100.0

            if (success) addGP(100)

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = minDuration - elapsed
            if (remaining > 0) delay(remaining)

            onResult(success)
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = minDuration - elapsed
            if (remaining > 0) delay(remaining)

            onResult(false)
        }
    }
}
