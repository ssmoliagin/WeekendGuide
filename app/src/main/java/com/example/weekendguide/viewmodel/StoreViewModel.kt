package com.example.weekendguide.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first

class StoreViewModel(
    application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val dataRepository: DataRepository,
) : AndroidViewModel(application) {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _regionsByCountry = MutableStateFlow<Map<String, List<Region>>>(emptyMap())
    val regionsByCountry: StateFlow<Map<String, List<Region>>> = _regionsByCountry.asStateFlow()

    private val _purchasedRegions = MutableStateFlow<Set<String>>(emptySet())
    val purchasedRegions: StateFlow<Set<String>> = _purchasedRegions.asStateFlow()

    private val _purchasedCountries = MutableStateFlow<Set<String>>(emptySet())
    val purchasedCountries: StateFlow<Set<String>> = _purchasedCountries.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    init {
        loadCountriesAndRegions()
        loadPurchased()
    }

    private fun loadPurchased() { //?
        viewModelScope.launch {
            _purchasedRegions.value = userPreferences.getPurchasedRegions()
            _purchasedCountries.value = userPreferences.getPurchasedCountries()
        }
    }

    private fun loadCountriesAndRegions() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val countriesList = dataRepository.getCountries()
                val deferredRegions = countriesList.map { country ->
                    async {
                        country.countryCode to dataRepository.getRegions(country.countryCode)
                    }
                }
                val regionsMap = deferredRegions.awaitAll().toMap()

                _countries.value = countriesList
                _regionsByCountry.value = regionsMap
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Функция покупки региона с загрузкой POI и обновлением списка
    fun purchaseRegionAndLoadPOI(region: Region, translateViewModel: TranslateViewModel) {
        viewModelScope.launch {
            try {
                // Сохраняем регион и страну локально
                userPreferences.addPurchasedRegion(region.region_code)
                userPreferences.addPurchasedCountries(region.country_code)

                // 🔁 Обновляем Firestore
                val currentData = userPreferences.userDataFlow.first()
                val updatedData = currentData.copy(
                    purchasedRegions = currentData.purchasedRegions + region.region_code,
                    purchasedCountries = currentData.purchasedCountries + region.country_code
                )
                userPreferences.saveUserData(updatedData)
                userRemote.launchSyncLocalToRemote(viewModelScope)

                // Скачиваем файл POI
                dataRepository.downloadAndCachePOI(region, translateViewModel)

                // Загружаем POI из файла
                val pois = dataRepository.getPOIs(region.region_code, translateViewModel)
                _poiList.value = pois

                // Обновляем список купленных
                loadPurchased()

            } catch (e: Exception) {
                Log.e("StoreViewModel", "Ошибка при покупке региона и загрузке POI", e)
            }
        }
    }
}
