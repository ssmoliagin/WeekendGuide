package com.example.weekendguide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.weekendguide.data.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserData(
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val language: String? = null,
    val userThema: String? = null,

)

class UserPreferences(private val context: Context) {

    private object Keys {
        val EMAIL = stringPreferencesKey("user_email")
        val DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val PHOTO_URL = stringPreferencesKey("user_photo_url")
        val LANGUAGE = stringPreferencesKey("language_code")
        val THEME = stringPreferencesKey("app_theme")

        val HOME_REGIONS = stringPreferencesKey("home_region") // JSON-строка Region
        val PURCHASED_REGIONS = stringSetPreferencesKey("purchased_regions")
        val PURCHASED_COUNTRIES = stringSetPreferencesKey("purchased_countries")
        val CURRENT_CITY = stringPreferencesKey("current_city")
        val LAT = doublePreferencesKey("current_lat")
        val LNG = doublePreferencesKey("current_lng")
        val FAVORITES = stringSetPreferencesKey("favorite_poi_ids")
        val VISITED = stringSetPreferencesKey("visited_poi_ids")
        val CATEGORY_LEVELS = stringPreferencesKey("category_levels")
    }

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    //login
    val userDataFlow: Flow<UserData> = context.dataStore.data
        .map { prefs ->
            UserData(
                email = prefs[Keys.EMAIL],
                displayName = prefs[Keys.DISPLAY_NAME],
                photoUrl = prefs[Keys.PHOTO_URL],
                language = prefs[Keys.LANGUAGE]
            )
        }

    suspend fun saveUserInfo(userData: UserData) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EMAIL] = userData.email ?: ""
            prefs[Keys.DISPLAY_NAME] = userData.displayName ?: ""
            prefs[Keys.PHOTO_URL] = userData.photoUrl ?: ""
            prefs[Keys.LANGUAGE] = userData.language ?: ""
        }
    }

    suspend fun clearUserInfo() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.DISPLAY_NAME)
            prefs.remove(Keys.PHOTO_URL)
        }
    }

    //смена темы
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme
        }
    }
    suspend fun getTheme(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.THEME] ?: "light" // по умолчанию светлая тема
    }

    //Методы для получения и сохранения уровней

    suspend fun getCategoryLevels(): Map<String, Int> {
        val prefs = context.dataStore.data.first()
        val json = prefs[Keys.CATEGORY_LEVELS] ?: return emptyMap()
        return Json.decodeFromString(json)
    }

    suspend fun saveCategoryLevels(map: Map<String, Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CATEGORY_LEVELS] = Json.encodeToString(map)
        }
    }

    suspend fun getLevelForCategory(category: String): Int {
        return getCategoryLevels()[category] ?: 0
    }

    suspend fun levelUpCategory(category: String, newLevel: Int, rewardPoints: Int) {
        val levels = getCategoryLevels().toMutableMap()
        levels[category] = newLevel
        saveCategoryLevels(levels)
        addGP(rewardPoints)
    }

    //Методы для получения и сохранения очков

    suspend fun getCurrentGP(): Int = prefs.getInt("current_gp", 0)
    fun getTotalGP(): Int = prefs.getInt("total_gp", 0)


    suspend fun addGP(points: Int) {
        val current = getCurrentGP()
        val total = getTotalGP()
        prefs.edit()
            .putInt("current_gp", current + points)
            .putInt("total_gp", total + points)
            .apply()
    }

    suspend fun spentGP(points: Int): Boolean {
        val current = getCurrentGP()
        val spent = getSpentGP()
        return if (current >= points) {
            prefs.edit()
                .putInt("current_gp", current - points)
                .putInt("spent_gp", spent + points)
                .apply()
            true
        } else {
            false
        }
    }

    fun getSpentGP(): Int = prefs.getInt("spent_gp", 0)

    suspend fun resetGP() {
        prefs.edit()
            .putInt("current_gp", 0)
            .putInt("total_gp", 0)
            .putInt("spent_gp",0)
            .apply()
    }


    //язык
    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun getLanguage(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.LANGUAGE] ?: ""
    }

    suspend fun saveHomeRegion(region: Region) {
        context.dataStore.edit { prefs ->
            // Получаем текущий список регионов из DataStore
            val currentJson = prefs[Keys.HOME_REGIONS]
            val currentList = if (currentJson != null) {
                Json.decodeFromString<List<Region>>(currentJson)
            } else {
                emptyList()
            }

            // Добавляем новый регион, если его ещё нет в списке
            val updatedList = if (region !in currentList) {
                currentList + region
            } else {
                currentList
            }

            // Сохраняем обновлённый список обратно
            prefs[Keys.HOME_REGIONS] = Json.encodeToString(updatedList)
        }
    }

    suspend fun getHomeRegions(): List<Region> {
        val prefs = context.dataStore.data.first()
        val json = prefs[Keys.HOME_REGIONS] ?: return emptyList()
        return Json.decodeFromString(json)
    }



    suspend fun addPurchasedRegion(regionCode: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PURCHASED_REGIONS] ?: emptySet()
            prefs[Keys.PURCHASED_REGIONS] = current + regionCode
        }
    }

    suspend fun addPurchasedCountries(countryCode: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PURCHASED_COUNTRIES] ?: emptySet()
            prefs[Keys.PURCHASED_COUNTRIES] = current + countryCode
        }
    }

    suspend fun getPurchasedCountries(): Set<String> {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.PURCHASED_COUNTRIES] ?: emptySet()
    }

    suspend fun getPurchasedRegions(): Set<String> {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.PURCHASED_REGIONS] ?: emptySet()
    }

    suspend fun saveCurrentCity(city: String) {
        context.dataStore.edit { prefs -> prefs[Keys.CURRENT_CITY] = city }
    }

    suspend fun getCurrentCity(): String? {
        return context.dataStore.data.first()[Keys.CURRENT_CITY]
    }

    suspend fun saveCurrentLocation(lat: Double, lng: Double) {
        context.dataStore.edit {
            it[Keys.LAT] = lat
            it[Keys.LNG] = lng
        }
    }

    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        val data = context.dataStore.data.first()
        val lat = data[Keys.LAT]
        val lng = data[Keys.LNG]
        return if (lat != null && lng != null) Pair(lat, lng) else null
    }

    // Получить текущее множество избранного
    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val currentFavorites = prefs[Keys.FAVORITES] ?: emptySet()
            prefs[Keys.FAVORITES] = if (currentFavorites.contains(id)) {
                currentFavorites - id
            } else {
                currentFavorites + id
            }
        }
    }

    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.FAVORITES] ?: emptySet() }

    //посещенные
    val visitedIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.VISITED] ?: emptySet() }

    suspend fun markVisited(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.VISITED] ?: emptySet()
            prefs[Keys.VISITED] = current + id
        }
    }

    /*
    data class UserSettings(
    val homeRegion: Region?,
    val purchasedRegions: Set<String>,
    val purchasedCountries: Set<String>,
    val currentCity: String?,
    val currentLocation: Pair<Double, Double>?,
    val favoritePoiIds: Set<String>,
    val visitedPoiIds: Set<String>,
    val currentGP: Int,
    val totalGP: Int,
    val categoryLevels: Map<String, Int>
)

    suspend fun getAll(): UserSettings {
        val prefs = context.dataStore.data.first()

        val language = prefs[Keys.LANGUAGE] ?: "de"
        val homeRegionJson = prefs[Keys.HOME_REGION]
        val homeRegion = homeRegionJson?.let { Json.decodeFromString<Region>(it) }
        val purchasedRegions = prefs[Keys.PURCHASED_REGIONS] ?: emptySet()
        val purchasedCountries = prefs[Keys.PURCHASED_COUNTRIES] ?: emptySet()
        val currentCity = prefs[Keys.CURRENT_CITY]
        val lat = prefs[Keys.LAT]
        val lng = prefs[Keys.LNG]
        val currentLocation = if (lat != null && lng != null) Pair(lat, lng) else null
        val favoritePoiIds = prefs[Keys.FAVORITES] ?: emptySet()
        val visitedPoiIds = prefs[Keys.VISITED] ?: emptySet()
        val currentGP = getCurrentGP()
        val totalGP = getTotalGP()

        val categoryLevelsJson = prefs[Keys.CATEGORY_LEVELS]
        val categoryLevels = categoryLevelsJson?.let { Json.decodeFromString<Map<String, Int>>(it) } ?: emptyMap()

        return UserSettings(
            language = language,
            homeRegion = homeRegion,
            purchasedRegions = purchasedRegions,
            purchasedCountries = purchasedCountries,
            currentCity = currentCity,
            currentLocation = currentLocation,
            favoritePoiIds = favoritePoiIds,
            visitedPoiIds = visitedPoiIds,
            currentGP = currentGP,
            totalGP = totalGP,
            categoryLevels = categoryLevels
        )
    }

     */

}