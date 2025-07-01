package com.example.weekendguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.WikiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class POIViewModel(
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val wikiRepository: WikiRepository,
    private val region: Region,
    private val userPreferences: UserPreferences
) : ViewModel() {

    //посещенные
    private val _visitedPoiIds = MutableStateFlow<Set<String>>(emptySet())
    val visitedPoiIds: StateFlow<Set<String>> = _visitedPoiIds.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.visitedIdsFlow.collect {
                _visitedPoiIds.value = it
            }
        }
    }

    fun isPoiVisited(poiId: String): Boolean {
        return visitedPoiIds.value.contains(poiId)
    }

    fun markPoiVisited(poiId: String) {
        viewModelScope.launch {
            userPreferences.markVisited(poiId)
        }
    }

    //ВИКИПЕДИЯ

    private val _wikiDescription = MutableStateFlow<String?>(null)
    val wikiDescription: StateFlow<String?> = _wikiDescription

    fun loadWikipediaDescription(title: String) {
        viewModelScope.launch {
            _wikiDescription.value = wikiRepository.fetchWikipediaDescription(title)
        }
    }

    //


    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    private val _maxDistance = MutableStateFlow(100)

    // Подписка на избранное из UserPreferences
    val favoriteIds: StateFlow<Set<String>> = userPreferences.favoriteIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Отфильтрованные POI по запросу
    val filteredPOIs: StateFlow<List<POI>> = combine(_poiList, _searchQuery, _maxDistance) { pois, query, _ ->
        if (query.isBlank()) pois
        else pois.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPOIs(translateViewModel)
    }

    fun loadPOIs(translateViewModel: TranslateViewModel) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dataRepository.downloadAndCachePOI(region, translateViewModel)
                val pois = dataRepository.getPOIs(region.region_code, translateViewModel)
                _poiList.value = pois
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(poiId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(poiId)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateMaxDistance(distance: Int) {
        _maxDistance.value = distance
    }

    fun getPOIById(poiId: String): POI? {
        return _poiList.value.find { it.id == poiId }
    }
}


