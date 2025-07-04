package com.example.weekendguide.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepositoryImpl(application)
    private val userPreferences = UserPreferences(application)

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

    init {
        loadCountriesAndRegions()
        loadPurchased()
    }

    private fun loadPurchased() {
        viewModelScope.launch {
            _purchasedRegions.value = userPreferences.getPurchasedRegions()
            _purchasedCountries.value = userPreferences.getPurchasedCountries()
        }
    }

    fun purchaseRegion(region: Region, translateViewModel: TranslateViewModel) {
        viewModelScope.launch {
            userPreferences.addPurchasedRegion(region.region_code)
            userPreferences.addPurchasedCountries(region.country_code)
            loadPurchased()
            downloadAndCacheRegionPOI(region, translateViewModel)
        }
    }

    fun downloadAndCacheRegionPOI(region: Region, translateViewModel: TranslateViewModel) {
        viewModelScope.launch {
            try {
                repository.downloadAndCachePOI(region, translateViewModel)
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки POI: ${e.localizedMessage}"
            }
        }
    }

    private fun loadCountriesAndRegions() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val countriesList = repository.getCountries()
                val regionsMap = mutableMapOf<String, List<Region>>()
                for (country in countriesList) {
                    val regionsList = repository.getRegions(country.countryCode)
                    regionsMap[country.countryCode] = regionsList
                }
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


//

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    // Функция покупки региона с загрузкой POI и обновлением списка
    fun purchaseRegionAndLoadPOI(region: Region, translateViewModel: TranslateViewModel) {
        viewModelScope.launch {
            try {
                userPreferences.addPurchasedRegion(region.region_code)
                userPreferences.addPurchasedCountries(region.country_code)

                // Скачиваем файл POI
                repository.downloadAndCachePOI(region, translateViewModel)

                // Загружаем POI из файла
                val pois = repository.getPOIs(region.region_code, translateViewModel)

                // Обновляем
                _poiList.value = pois

                // Также можно обновить купленные регионы
                loadPurchased()
            } catch (e: Exception) {
                // Обработка ошибок
                Log.e("RegionViewModel", "Ошибка при покупке региона и загрузке POI", e)
            }
        }
    }

}
