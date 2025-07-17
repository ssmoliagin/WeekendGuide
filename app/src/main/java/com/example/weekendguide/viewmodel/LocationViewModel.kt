package com.example.weekendguide.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationViewModel(
    application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(application) {

    private val _currentCity = MutableStateFlow<String?>(null)
    val currentCity = _currentCity.asStateFlow()

    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location = _location.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    init {
        loadSavedLocation()
    }

    fun loadSavedLocation() {
        viewModelScope.launch {
            _currentCity.value = userPreferences.getCurrentCity()
            _location.value = userPreferences.getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun detectLocationFromGPS() {
        viewModelScope.launch {
            try {
                val tokenSource = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    tokenSource.token
                ).await()

                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    _location.value = lat to lng
                    userPreferences.saveCurrentLocation(lat, lng)

                    val cityName = getCityName(lat, lng)
                    cityName?.let {
                        _currentCity.value = it
                        userPreferences.saveCurrentCity(it)

                        val currentData = userPreferences.userDataFlow.first()
                        val updatedData = currentData.copy(
                            currentCity = it,
                            currentLat = lat,
                            currentLng = lng
                        )
                        userPreferences.saveUserData(updatedData)
                        userRemote.launchSyncLocalToRemote(viewModelScope)
                    }
                }
            } catch (_: Exception) {
                // Optionally handle error silently or emit to error state
            }
        }
    }

    private fun getCityName(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()?.locality
        } catch (_: Exception) {
            null
        }
    }

    fun setManualLocation(city: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _location.value = lat to lng
            _currentCity.emit(city)

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(
                currentCity = city,
                currentLat = lat,
                currentLng = lng
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}
