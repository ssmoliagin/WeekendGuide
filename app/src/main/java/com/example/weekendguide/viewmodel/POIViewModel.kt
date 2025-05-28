package com.example.weekendguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.repository.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.weekendguide.data.model.Region

class POIViewModel(
    private val dataRepository: DataRepository,
    private val region: Region
) : ViewModel() {

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPOIs()
    }

    fun loadPOIs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dataRepository.downloadAndCachePOI(region)

                // 🔁 Изменено: теперь метод принимает только regionCode
                val pois = dataRepository.getPOIs(region.region_code)

                _poiList.value = pois
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}