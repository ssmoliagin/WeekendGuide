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
    private val region: List<Region>,
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
    private var lastWikiTitle: String? = null

    // --- Список POI и состояние загрузки ---
    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _poisIsLoading = MutableStateFlow(false)
    val poisIsLoading: StateFlow<Boolean> = _poisIsLoading

    // --- Избранное ---
    val favoriteIds: StateFlow<Set<String>> = userPreferences.favoriteIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- Типы POI и состояние загрузки ---
    private val _typesIsLoading = MutableStateFlow(false)
    val typesIsLoading: StateFlow<Boolean> = _typesIsLoading

    private val _allTypes = MutableStateFlow<List<String>>(emptyList())
    val allTypes: StateFlow<List<String>> = _allTypes

    // --- Инициализация ---
    init {
        // Слушаем изменения посещённых
        viewModelScope.launch {
            userPreferences.visitedIdsFlow.collect {
                _visitedPoiIds.value = it
            }
        }

        // Слушаем смену языка и перезагружаем POI и переводы
        viewModelScope.launch {
            translateViewModel.language.collect {
                loadTypePOITranslations()
                loadPOIs()
            }
        }

        // Начальная загрузка
        loadTypePOITranslations()
        loadPOIs()
        observeLanguageChanges()
    }

    // --- Функции загрузки и обновления данных POI ---

    fun loadPOIs() {
        viewModelScope.launch {
            _poisIsLoading.value = true
            try {
                val allPOIs = mutableListOf<POI>()

                // Для каждого региона загружаем и кэшируем POI
                for (reg in region) {
                    dataRepository.downloadAndCachePOI(reg, translateViewModel)

                    val pois = dataRepository.getPOIs(reg.region_code, translateViewModel)
                    allPOIs += pois
                }

                _poiList.value = allPOIs
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
            } finally {
                _poisIsLoading.value = false
            }
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

    // --- Функции работы с Wikipedia API ---

    fun loadWikipediaDescription(title: String) {
        lastWikiTitle = title
        _wikiDescription.value = null // <--- очищаем перед загрузкой

        viewModelScope.launch {
            val result = wikiRepository.fetchWikipediaDescription(title, translateViewModel.language.value)
            // Обновляем только если это всё ещё актуальный запрос
            if (lastWikiTitle == title) {
                _wikiDescription.value = result
            }
        }
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            translateViewModel.language.collect { newLang ->
                lastWikiTitle?.let { title ->
                    _wikiDescription.value = null // очищаем перед загрузкой
                    val result = wikiRepository.fetchWikipediaDescription(title, newLang)
                    if (lastWikiTitle == title) {
                        _wikiDescription.value = result
                    }
                }
            }
        }
    }

    // --- Функции работы с избранными POI ---
    fun toggleFavorite(poiId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(poiId)
        }
    }

    // --- Функции работы с посещёнными POI ---
    fun markPoiVisited(poiId: String) {
        viewModelScope.launch {
            userPreferences.markVisited(poiId)
        }
    }
}
