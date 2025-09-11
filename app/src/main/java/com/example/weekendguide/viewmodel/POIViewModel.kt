package com.example.weekendguide.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class POIViewModelFactory(
    private val region: List<Region>,
    private val subscriptionRegions: List<String>,
    private val isSubscription: Boolean,
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(POIViewModel::class.java)) {
            val wikiRepository = WikiRepositoryImp()
            return POIViewModel(region, subscriptionRegions, isSubscription, translateViewModel, dataRepository, userPreferences, userRemote, wikiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class POIViewModel(
    private val region: List<Region>,
    private val subscriptionRegions: List<String>,
    private val isSubscription: Boolean,
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val wikiRepository: WikiRepository,
) : ViewModel() {

    val language = translateViewModel.language.value


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
        viewModelScope.launch { loadAllReviews() }
        viewModelScope.launch { translateViewModel.language.collect { loadAllPOIData() } }
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
                val filteredRegions = if (isSubscription) {
                    region
                } else {
                    region.filter { reg -> reg.region_code !in subscriptionRegions }
                }

                val allPOIs = mutableListOf<POI>()
                for (reg in filteredRegions) {
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
            userPreferences.updateVisitedPOIs(poiId, review = false)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun confirmPoiVisited(poiId: String) {
        viewModelScope.launch {
            userPreferences.updateVisitedPOIs(poiId, review = true)
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
        confirmPoiVisited(review.poiId)
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

    //Share and Save POI
    fun sharePOI(context: Context, poi: POI, localizedTitle: String, localizedDescription: String, isSubscription: Boolean) {
        viewModelScope.launch {

            if (!isSubscription) {
                val today = getTodayUtc()
                val lastDate = userPreferences.getLastShareDate()

                if (lastDate != null && lastDate == today) {
                    Toast.makeText(context, LocalizerUI.t("radius_with_subscription_only", language), Toast.LENGTH_SHORT).show()
                    return@launch
                } else {
                    userPreferences.setLastShareDate(today)
                }
            }

            val description = localizedDescription
                .lines()
                .joinToString("\n") { it.trimStart() }
                .trim()

            val locationUrl = "https://maps.google.com/?q=${poi.lat},${poi.lng}"

            val shareText = buildString {
                append("📍 $localizedTitle\n")
                append(description.trim())
                append("\n\n🤳 ${LocalizerUI.t("share_by", language)}\n\n")
                append("🌍 $locationUrl")
            }

            val imageUri = withContext(Dispatchers.IO) {
                poi.imageUrl?.let { localPath ->
                    try {
                        val originalFile = File(localPath)
                        if (originalFile.exists()) {
                            val tempFile = File(context.cacheDir, "shared_${poi.id}.jpg")
                            originalFile.copyTo(tempFile, overwrite = true)
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                tempFile
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = if (imageUri != null) "image/*" else "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                imageUri?.let {
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            context.startActivity(Intent.createChooser(intent, "Share to"))
        }
    }

    suspend fun saveAsGpx(context: Context, poi: POI, isSubscription: Boolean) {
        try {

            if (!isSubscription) {
                val today = getTodayUtc()
                val lastDate = userPreferences.getLastSaveGpxDate()

                if (lastDate != null && lastDate == today) {
                    Toast.makeText(
                        context,
                        LocalizerUI.t("radius_with_subscription_only", language),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                } else {
                    userPreferences.setLastSaveGpxDate(today)
                }
            }

            val gpxContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="WeekendGuide" xmlns="http://www.topografix.com/GPX/1/1">
                <wpt lat="${poi.lat}" lon="${poi.lng}">
                    <name>${poi.title}</name>
                </wpt>
            </gpx>
        """.trimIndent()

            val fileName = "poi_${poi.id}.gpx"
            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/gpx+xml")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(gpxContent.toByteArray())
                    }
                    Toast.makeText(context, "Saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save GPX file", Toast.LENGTH_SHORT).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                file.writeText(gpxContent)
                Toast.makeText(context, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving GPX file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTodayUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
