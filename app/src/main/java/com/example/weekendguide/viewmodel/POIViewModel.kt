package com.example.weekendguide.viewmodel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.data.repository.WikiRepository
import com.example.weekendguide.data.repository.WikiRepositoryImp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class POIViewModelFactory(
    private val region: List<Region>,
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(POIViewModel::class.java)) {
            val wikiRepository = WikiRepositoryImp()
            return POIViewModel(translateViewModel, dataRepository, region, userPreferences, userRemote, wikiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class POIViewModel(
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val region: List<Region>,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val wikiRepository: WikiRepository,
) : ViewModel() {

    val language = translateViewModel.language.value

    private val _visitedPoiIds = MutableStateFlow<Set<String>>(emptySet())
    val visitedPoiIds: StateFlow<Set<String>> = _visitedPoiIds.asStateFlow()

    private val _wikiDescription = MutableStateFlow<String?>(null)
    val wikiDescription: StateFlow<String?> = _wikiDescription
    private var lastWikiTitle: String? = null

    private val _wikiAnnotatedDescription = MutableStateFlow<AnnotatedString?>(null)
    val wikiAnnotatedDescription: StateFlow<AnnotatedString?> = _wikiAnnotatedDescription

    private val _poiList = MutableStateFlow<List<POI>>(emptyList())
    val poiList: StateFlow<List<POI>> = _poiList

    private val _poisIsLoading = MutableStateFlow(false)
    val poisIsLoading: StateFlow<Boolean> = _poisIsLoading

    val favoriteIds: StateFlow<Set<String>> = userPreferences.favoriteIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _typesIsLoading = MutableStateFlow(false)
    val typesIsLoading: StateFlow<Boolean> = _typesIsLoading

    private val _allTypes = MutableStateFlow<List<String>>(emptyList())
    val allTypes: StateFlow<List<String>> = _allTypes

    private val _tagsIsLoading = MutableStateFlow(false)
    val tagsIsLoading: StateFlow<Boolean> = _tagsIsLoading

    private val _allTags = MutableStateFlow<List<String>>(emptyList())
    val allTags: StateFlow<List<String>> = _allTags

    private val _userReviews = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val userReviews: StateFlow<Map<String, Boolean>> = _userReviews

    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<Review>>> = _reviews

    init {
        viewModelScope.launch {
            launch {
                loadAllReviews()
                userPreferences.visitedIdsFlow.collect {
                    _visitedPoiIds.value = it
                }
            }

            launch {
                translateViewModel.language
                    .collect { loadAllPOIData() }
            }
        }

        observeLanguageChanges()
    }

    private fun loadAllPOIData() {
        loadTypePOITranslations()
        loadTagsPOITranslations()
        loadPOIs()
    }

    fun loadPOIs() {
        viewModelScope.launch {
            _poisIsLoading.value = true
            try {
                val allPOIs = mutableListOf<POI>()
                for (reg in region) {
                    dataRepository.downloadAndCachePOI(reg)
                    val pois = dataRepository.getPOIs(reg.region_code)
                    allPOIs += pois
                }
                _poiList.value = allPOIs
            } finally {
                _poisIsLoading.value = false
            }
        }
    }

    fun loadTypePOITranslations() {
        viewModelScope.launch {
            _typesIsLoading.value = true
            try {
                val cached = dataRepository.getTypes()
                if (cached != null) {
                    LocalizerUI.loadFromJson(cached)
                    val parsed = JSONObject(cached)
                    _allTypes.value = parsed.keys().asSequence().toList()
                }

                val downloaded = dataRepository.downloadTypesJson()
                if (downloaded != null) {
                    LocalizerUI.loadFromJson(downloaded)
                    val parsed = JSONObject(downloaded)
                    _allTypes.value = parsed.keys().asSequence().toList()
                }
            } finally {
                _typesIsLoading.value = false
            }
        }
    }

    fun loadTagsPOITranslations() {
        viewModelScope.launch {
            _tagsIsLoading.value = true
            try {
                val cached = dataRepository.getTags()
                if (cached != null) {
                    LocalizerUI.loadFromJson(cached)
                    val parsed = JSONObject(cached)
                    _allTags.value = parsed.keys().asSequence().toList()
                }

                val downloaded = dataRepository.downloadTagsJson()
                if (downloaded != null) {
                    LocalizerUI.loadFromJson(downloaded)
                    val parsed = JSONObject(downloaded)
                    _allTags.value = parsed.keys().asSequence().toList()
                }
            } finally {
                _tagsIsLoading.value = false
            }
        }
    }

    fun loadWikipediaDescription(title: String) {
        lastWikiTitle = title
        _wikiDescription.value = null
        _wikiAnnotatedDescription.value = null

        viewModelScope.launch {
            val result = wikiRepository.fetchWikipediaDescription(title, translateViewModel.language.value)
            if (lastWikiTitle == title) {
                _wikiDescription.value = result
                result?.let {
                    _wikiAnnotatedDescription.value = buildAnnotatedDescription(it)
                }
            }
        }
    }

    private fun buildAnnotatedDescription(text: String): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("""\[([^\]]+)]\((https?://[^\)]+)\)""")
            val match = regex.find(text)

            if (match != null) {
                val startIndex = match.range.first

                val beforeLink = text.substring(0, startIndex)
                val linkText = match.groupValues[1]
                val linkUrl = match.groupValues[2]

                append(beforeLink)

                val start = length
                append(linkText)
                val end = length

                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )

                addStringAnnotation(
                    tag = "URL",
                    annotation = linkUrl,
                    start = start,
                    end = end
                )
            } else {
                append(text)
            }
        }
    }


    private fun observeLanguageChanges() {
        viewModelScope.launch {
            translateViewModel.language.collect { newLang ->
                lastWikiTitle?.let { title ->
                    _wikiDescription.value = null
                    val result = wikiRepository.fetchWikipediaDescription(title, newLang)
                    if (lastWikiTitle == title) {
                        _wikiDescription.value = result
                    }
                }
            }
        }
    }

    fun toggleFavorite(poiId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(poiId)
            val favorites = userPreferences.favoriteIdsFlow.first()
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(favorites = favorites.toList())
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun markPoiVisited(poiId: String) {
        viewModelScope.launch {
            userPreferences.markVisited(poiId)
            val visited = userPreferences.visitedIdsFlow.first()
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(visited = visited.toList())
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun loadReviews(poiId: String) {
        viewModelScope.launch {
            val loadedReviews = userRemote.getReviewsForPoi(poiId)
            _reviews.update { current ->
                current.toMutableMap().apply { put(poiId, loadedReviews) }
            }
        }
    }

    fun submitReview(review: Review, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                userRemote.submitReview(review)
                loadReviews(review.poiId)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun loadAllReviews() {
        viewModelScope.launch {
            val loadedList = userRemote.getAllReviews()
            _reviews.value = loadedList.groupBy { it.poiId }
        }
    }

    fun hasUserReviewed(poiId: String, userId: String): Boolean {
        return _reviews.value[poiId]?.any { it.userId == userId } == true
    }

    fun checkIfUserReviewed(poiId: String, userId: String) {
        val reviewed = hasUserReviewed(poiId, userId)
        _userReviews.value = _userReviews.value + (poiId to reviewed)
    }
}
