package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.model.Country
import com.weekendguide.app.data.model.Region
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.DataRepository
import com.weekendguide.app.data.repository.DataRepositoryImpl
import com.weekendguide.app.data.repository.UserRemoteDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun purchaseRegionAndLoadPOI(region: Region, isSubscription: Boolean, cost: Int) {
        viewModelScope.launch {
            try {
                userPreferences.addRegionInCollection(region)

                val currentData = userPreferences.userDataFlow.first()
                val updatedData = currentData.copy(
                    subscriptionRegions = if (isSubscription) (currentData.subscriptionRegions + region.region_code).toSet().toList()
                    else currentData.subscriptionRegions,
                    collectionRegions = if (region !in currentData.collectionRegions) {
                        currentData.collectionRegions + region
                    } else {
                        currentData.collectionRegions
                    },
                    current_GP = currentData.current_GP - cost,
                    spent_GP = currentData.spent_GP + cost,
                )
                userPreferences.saveUserData(updatedData)
                userRemote.launchSyncLocalToRemote(viewModelScope)

                //dataRepo.downloadAndCachePOI(region)

            } catch (e: Exception) {
                _error.value = "Loading error: ${e.message}"
            }
        }
    }
}
