package com.example.weekendguide.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class PointsViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PointsViewModel::class.java)) {
            return PointsViewModel(userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PointsViewModel(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    fun addGP(amount: Int) = viewModelScope.launch {

        val (current, total) = userPreferences.addGPAndGet(amount)

        val currentData = userPreferences.userDataFlow.first()
        val updatedData = currentData.copy(
            current_GP = current,
            total_GP = total
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
            val success = distanceMeters < 200.0

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
