package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.repository.DataRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepositoryImpl(application)

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _regionsByCountry = MutableStateFlow<Map<String, List<Region>>>(emptyMap())
    val regionsByCountry: StateFlow<Map<String, List<Region>>> = _regionsByCountry.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCountriesAndRegions()
    }

    fun downloadAndCacheRegionPOI(region: Region) {
        viewModelScope.launch {
            try {
                repository.downloadAndCachePOI(region)
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
}
