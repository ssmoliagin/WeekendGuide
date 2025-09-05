package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val dataRepo: DataRepositoryImpl,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(userPreferences, userRemote, dataRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class StoreViewModel(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val dataRepo: DataRepository,
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _regionsByCountry = MutableStateFlow<Map<String, List<Region>>>(emptyMap())
    val regionsByCountry: StateFlow<Map<String, List<Region>>> = _regionsByCountry.asStateFlow()

    val userData: StateFlow<UserData> = userPreferences.userDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserData()
    )

    private val _purchasedRegions = MutableStateFlow<Set<String>>(emptySet())
    //val purchasedRegions: StateFlow<Set<String>> = _purchasedRegions.asStateFlow()

    private val _purchasedCountries = MutableStateFlow<Set<String>>(emptySet())
    //val purchasedCountries: StateFlow<Set<String>> = _purchasedCountries.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList.asStateFlow()

    init {
        loadCountriesAndRegions()
    }

    private fun loadCountriesAndRegions() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val countriesList = dataRepo.getCountries()
                val deferredRegions = countriesList.map { country ->
                    async {
                        country.countryCode to dataRepo.getRegions(country.countryCode)
                    }
                }
                val regionsMap = deferredRegions.awaitAll().toMap()

                _countries.value = countriesList
                _regionsByCountry.value = regionsMap
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Loading error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun purchaseRegionAndLoadPOI(region: Region) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentData = userPreferences.userDataFlow.first()
                val updatedData = currentData.copy(
                    purchasedRegions = currentData.purchasedRegions + region.region_code,
                    purchasedCountries = (currentData.purchasedCountries + region.country_code).toSet().toList(), // если Set - purchasedCountries = currentData.purchasedCountries + region.country_code,
                    collectionRegions = if (region !in currentData.collectionRegions) {
                        currentData.collectionRegions + region
                    } else {
                        currentData.collectionRegions
                    }
                )
                userPreferences.saveUserData(updatedData)
                userRemote.launchSyncLocalToRemote(viewModelScope)

                dataRepo.downloadAndCachePOI(region)

            } catch (e: Exception) {
                _error.value = "Loading error: ${e.message}"
            } finally {
                _loading.value = false
            }

        }
    }
}
