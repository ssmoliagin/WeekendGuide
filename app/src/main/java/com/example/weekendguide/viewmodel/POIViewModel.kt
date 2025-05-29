package com.example.weekendguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.repository.DataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class POIViewModel(
    private val dataRepository: DataRepository,
    private val region: Region
) : ViewModel() {

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    private val _maxDistance = MutableStateFlow(100)

    val filteredPOIs: StateFlow<List<POI>> = combine(_poiList, _searchQuery, _maxDistance) { pois, query, _ ->
        if (query.isBlank()) pois
        else pois.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPOIs()
    }

    fun loadPOIs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dataRepository.downloadAndCachePOI(region)
                val pois = dataRepository.getPOIs(region.region_code)
                _poiList.value = pois
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPOI(query: String, distance: Int) {
        _searchQuery.value = query
        _maxDistance.value = distance
    }

    fun shuffleRecommendations() {
        _poiList.value = _poiList.value.shuffled(Random(System.currentTimeMillis()))
    }
}
