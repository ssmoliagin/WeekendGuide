package com.example.weekendguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.locales.LocalizerTypes
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.WikiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class POIViewModel(
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val wikiRepository: WikiRepository,
    private val region: Region,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Язык для переводов и запросов (текущая выбранная локаль)
    val language = translateViewModel.language.value

    // --- Посещённые POI ---
    private val _visitedPoiIds = MutableStateFlow<Set<String>>(emptySet())
    val visitedPoiIds: StateFlow<Set<String>> = _visitedPoiIds.asStateFlow()

    // --- Википедия ---
    private val _wikiDescription = MutableStateFlow<String?>(null)
    val wikiDescription: StateFlow<String?> = _wikiDescription

    // --- Список POI и состояние загрузки ---
    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _poisIsLoading = MutableStateFlow(false)
    val poisIsLoading: StateFlow<Boolean> = _poisIsLoading

    // --- Поисковые параметры ---
    private val _searchQuery = MutableStateFlow("")
    private val _maxDistance = MutableStateFlow(100)

    // --- Избранное ---
    val favoriteIds: StateFlow<Set<String>> = userPreferences.favoriteIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- Отфильтрованные POI по запросу и расстоянию ---
    val filteredPOIs: StateFlow<List<POI>> = combine(_poiList, _searchQuery, _maxDistance) { pois, query, _ ->
        if (query.isBlank()) pois
        else pois.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Типы POI и состояние загрузки ---
    private val _typesIsLoading = MutableStateFlow(false)
    val typesIsLoading: StateFlow<Boolean> = _typesIsLoading

    private val _allTypes = MutableStateFlow<List<String>>(emptyList())
    val allTypes: StateFlow<List<String>> = _allTypes

    // --- Инициализация ---
    init {
        // Подписка на изменения посещённых POI из UserPreferences
        viewModelScope.launch {
            userPreferences.visitedIdsFlow.collect {
                _visitedPoiIds.value = it
            }
        }
        // Загрузка POI и типов
        loadTypePOITranslations()
        loadPOIs()
    }

    // --- Функции работы с посещёнными POI ---
    fun isPoiVisited(poiId: String): Boolean {
        return visitedPoiIds.value.contains(poiId)
    }

    fun markPoiVisited(poiId: String) {
        viewModelScope.launch {
            userPreferences.markVisited(poiId)
        }
    }

    // --- Функции загрузки и обновления данных ---

    fun loadPOIs() {
        viewModelScope.launch {
            _poisIsLoading.value = true
            try {
                dataRepository.downloadAndCachePOI(region, translateViewModel)
                val pois = dataRepository.getPOIs(region.region_code, translateViewModel)
                _poiList.value = pois
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
            } finally {
                _poisIsLoading.value = false
            }
        }
    }

    fun loadWikipediaDescription(title: String) {
        viewModelScope.launch {
            _wikiDescription.value = wikiRepository.fetchWikipediaDescription(title, language)
        }
    }

    fun loadTypePOITranslations() {
        viewModelScope.launch {
            _typesIsLoading.value = true
            try {
                // Пытаемся загрузить из кэша
                val cached = dataRepository.getTypes()

                if (cached != null) {
                    Log.d("POIViewModel", "Loaded cached type.json: $cached")
                    LocalizerTypes.loadFromJson(cached)
                    val parsed = JSONObject(cached)
                    _allTypes.value = parsed.keys().asSequence().toList()
                }

                // Скачиваем обновления, если есть
                val downloaded = dataRepository.downloadTypesJson()
                if (downloaded != null) {
                    Log.d("POIViewModel", "Downloaded types JSON: $downloaded")
                    LocalizerTypes.loadFromJson(downloaded)
                    val parsed = JSONObject(downloaded)
                    _allTypes.value = parsed.keys().asSequence().toList()
                }

            } catch (exception: Exception) {
                Log.e("POIViewModel", "Error loading Types", exception)
            } finally {
                _typesIsLoading.value = false
            }
        }
    }

    // --- Работа с избранным ---
    fun toggleFavorite(poiId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(poiId)
        }
    }

    // --- Обновление параметров поиска ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateMaxDistance(distance: Int) {
        _maxDistance.value = distance
    }

    // --- Получение POI по ID ---
    fun getPOIById(poiId: String): POI? {
        return _poiList.value.find { it.id == poiId }
    }
}
