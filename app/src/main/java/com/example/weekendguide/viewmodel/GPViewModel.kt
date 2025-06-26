package com.example.weekendguide.viewmodel

import android.app.Application
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GPViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)
    private val _gp = MutableStateFlow(0)
    val gp: StateFlow<Int> = _gp.asStateFlow()

    init {
        viewModelScope.launch {
            _gp.value = prefs.getCurrentGP()
        }
    }

    fun addGP(amount: Int) {
        viewModelScope.launch {
            prefs.addGP(amount)
            _gp.value = prefs.getCurrentGP()
        }
    }

    fun resetGP() {
        viewModelScope.launch {
            prefs.resetGP()
            _gp.value = 0
        }
    }

    /**
     * Проверяет расстояние до POI, и если пользователь рядом — начисляет GP.
     * @return true если успешно начислены очки, false — если слишком далеко.
     */
    suspend fun checkAndAwardGPForPOI(
        poi: POI,
        locationViewModel: LocationViewModel,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val oldLocation = locationViewModel.location.value

            // Запрашиваем новую GPS-локацию
            locationViewModel.detectLocationFromGPS()

            // Ждём изменения координат
            val newLocation = locationViewModel.location
                .filterNotNull()
                .dropWhile { it == oldLocation }
                .first()

            // Считаем дистанцию
            val result = FloatArray(1)
            Location.distanceBetween(
                newLocation.first,
                newLocation.second,
                poi.lat,
                poi.lng,
                result
            )
            val distanceMeters = result[0]

            if (distanceMeters < 200000000) {
                addGP(100)
                onResult(true)
            } else {
                onResult(false)
            }
        } catch (e: Exception) {
            onResult(false)
            e.printStackTrace()
        }
    }
}
