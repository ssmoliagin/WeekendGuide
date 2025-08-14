package com.example.weekendguide.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class LocationViewModelFactory(
    private val app: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(app, userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LocationViewModel(
    private val app: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(app) {

    private val _currentCity = MutableStateFlow<String?>(null)

    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location = _location.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)


    suspend fun setHomeLocation(): String? {
        val currentData = userPreferences.userDataFlow.first()

        val updatedData = currentData.copy(
            currentCity = currentData.homeCity,
            currentLat = currentData.homeLat,
            currentLng = currentData.homeLng,
        )

        userPreferences.saveUserData(updatedData)
        userRemote.launchSyncLocalToRemote(viewModelScope)

        _location.value = userPreferences.getCurrentLocation()
        return updatedData.currentCity
    }


    @SuppressLint("MissingPermission")
    suspend fun detectLocationFromGPS(): Pair<Double, Double>? {
        val tokenSource = CancellationTokenSource()
        val location = fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            tokenSource.token
        ).await()

        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude

            _location.value = lat to lng

            val cityName = getCityName(lat, lng)
            cityName?.let { currentCityName ->
                _currentCity.value = currentCityName

                val currentData = userPreferences.userDataFlow.first()

                val updatedData = currentData.copy(
                    currentCity = currentCityName,
                    currentLat = lat,
                    currentLng = lng,
                    homeCity = if (currentData.homeCity.isNullOrEmpty()) currentCityName else currentData.homeCity,
                    homeLat = currentData.homeLat ?: lat,
                    homeLng = currentData.homeLng ?: lng
                )
                userPreferences.saveUserData(updatedData)
                userRemote.launchSyncLocalToRemote(viewModelScope)
            }

            return lat to lng
        }

        return null
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

    fun setManualLocation(city: String, lat: Double, lng: Double, editHomeCity: Boolean) {
        viewModelScope.launch {
            _location.value = lat to lng
            _currentCity.emit(city)

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(
                currentCity = city,
                currentLat = lat,
                currentLng = lng,

                homeCity = if (currentData.homeCity.isNullOrEmpty() || editHomeCity) city else currentData.homeCity,
                homeLat = if (currentData.homeLat == null || editHomeCity) lat else currentData.homeLat,
                homeLng = if (currentData.homeLng == null || editHomeCity) lng else currentData.homeLng

            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}
