package com.example.weekendguide.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PointsViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource) : AndroidViewModel(application) {

    private val _current_gp = MutableStateFlow(0)
    private val _total_gp = MutableStateFlow(0)
    private val _spent_gp = MutableStateFlow(0)
    val currentGP: StateFlow<Int> = _current_gp.asStateFlow()
    val totalGP: StateFlow<Int> = _total_gp.asStateFlow()
    val spentGP: StateFlow<Int> = _spent_gp.asStateFlow()

    init {
        refreshGP()
    }

    fun refreshGP() {
        viewModelScope.launch {
            _current_gp.value = userPreferences.getCurrentGP()
            _total_gp.value = userPreferences.getTotalGP()
            _spent_gp.value = userPreferences.getSpentGP()
        }
    }

    //пополняем очки
    fun addGP(amount: Int) {
        viewModelScope.launch {
            userPreferences.addGP(amount)
            _current_gp.value = userPreferences.getCurrentGP()
            _total_gp.value = userPreferences.getTotalGP()

            // 🔁 Обновляем также в Firestore
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = UserData(
                current_GP = _current_gp.value,
                total_GP = _total_gp.value,
                spent_GP = _spent_gp.value
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    //тратим очки
    fun spentGP(amount: Int) {
        viewModelScope.launch {
            val success = userPreferences.spentGP(amount)
            if (success) {
                _current_gp.value = userPreferences.getCurrentGP()
                _spent_gp.value = userPreferences.getSpentGP()

                // 🔁 Обновляем также в Firestore
                val currentData = userPreferences.userDataFlow.first()
                val updatedData = UserData(
                    current_GP = _current_gp.value,
                    total_GP = _total_gp.value,
                    spent_GP = _spent_gp.value
                )
                userPreferences.saveUserData(updatedData)
                userRemote.launchSyncLocalToRemote(viewModelScope)
            } else {
                // Можно добавить LiveData/Event для UI: "Недостаточно очков"
            }
        }
    }

    //обнулить
    fun resetGP() {
        viewModelScope.launch {
            userPreferences.resetGP()
            _current_gp.value = 0
            _total_gp.value = 0
            _spent_gp.value = 0

            // 🔁 Обновляем также в Firestore
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = UserData(
                current_GP = _current_gp.value,
                total_GP = _total_gp.value,
                spent_GP = _spent_gp.value
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
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

            if (distanceMeters < 100_000_000) {
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
