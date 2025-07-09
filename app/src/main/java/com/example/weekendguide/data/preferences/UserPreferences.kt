package com.example.weekendguide.data.preferences

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private object Keys {
        val EMAIL = stringPreferencesKey("user_email")
        val DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val PHOTO_URL = stringPreferencesKey("user_photo_url")
        val LANGUAGE = stringPreferencesKey("language_code")
        val THEME = stringPreferencesKey("app_theme")
        val MEASURED = stringPreferencesKey("user_measurement")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val TOTAL_GP = intPreferencesKey("total_GP")
        val CURRENT_GP = intPreferencesKey("current_GP")
        val SPENT_GP = intPreferencesKey("spent_GP")

        val COLLECTION_REGIONS = stringPreferencesKey("collection_region")
        val PURCHASED_REGIONS = stringSetPreferencesKey("purchased_regions")
        val PURCHASED_COUNTRIES = stringSetPreferencesKey("purchased_countries")
        val CURRENT_CITY = stringPreferencesKey("current_city")
        val LAT = doublePreferencesKey("current_lat")
        val LNG = doublePreferencesKey("current_lng")
        val FAVORITES = stringSetPreferencesKey("favorite_poi_ids")
        val VISITED = stringSetPreferencesKey("visited_poi_ids")
        val CATEGORY_LEVELS = stringPreferencesKey("category_levels")
    }

    val userDataFlow: Flow<UserData> = context.dataStore.data
        .map { prefs ->
            val levelsJson = prefs[Keys.CATEGORY_LEVELS]
            val levels = if (levelsJson != null) Json.decodeFromString<Map<String, Int>>(levelsJson) else emptyMap()

            val collectionJson = prefs[Keys.COLLECTION_REGIONS]
            val collectionRegions = if (collectionJson != null) Json.decodeFromString<List<Region>>(collectionJson) else emptyList()

            UserData(
                email = prefs[Keys.EMAIL],
                displayName = prefs[Keys.DISPLAY_NAME],
                photoUrl = prefs[Keys.PHOTO_URL],
                language = prefs[Keys.LANGUAGE],
                userThema = prefs[Keys.THEME],
                userMeasurement = prefs[Keys.MEASURED],
                notification = prefs[Keys.NOTIFICATIONS],
                total_GP = prefs[Keys.TOTAL_GP] ?: 0,
                current_GP = prefs[Keys.CURRENT_GP] ?: 0,
                spent_GP = prefs[Keys.SPENT_GP] ?: 0,
                categoryLevels = levels,
                collectionRegions = collectionRegions,
                purchasedRegions = prefs[Keys.PURCHASED_REGIONS]?.toList() ?: emptyList(),
                purchasedCountries = prefs[Keys.PURCHASED_COUNTRIES]?.toList() ?: emptyList(),
                currentCity = prefs[Keys.CURRENT_CITY],
                currentLat = prefs[Keys.LAT],
                currentLng = prefs[Keys.LNG],
                favorites = prefs[Keys.FAVORITES]?.toList() ?: emptyList(),
                visited = prefs[Keys.VISITED]?.toList() ?: emptyList(),
            )
        }

    suspend fun saveUserData(userData: UserData) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EMAIL] = userData.email ?: ""
            prefs[Keys.DISPLAY_NAME] = userData.displayName ?: ""
            prefs[Keys.PHOTO_URL] = userData.photoUrl ?: ""
            prefs[Keys.LANGUAGE] = userData.language ?: ""
            prefs[Keys.THEME] = userData.userThema ?: "light"
            prefs[Keys.MEASURED] = userData.userMeasurement ?: "km"
            prefs[Keys.NOTIFICATIONS] = userData.notification ?: true
            prefs[Keys.TOTAL_GP] = userData.total_GP
            prefs[Keys.CURRENT_GP] = userData.current_GP
            prefs[Keys.SPENT_GP] = userData.spent_GP

            prefs[Keys.CATEGORY_LEVELS] = Json.encodeToString(userData.categoryLevels)
            prefs[Keys.COLLECTION_REGIONS] = Json.encodeToString(userData.collectionRegions)
            prefs[Keys.PURCHASED_REGIONS] = userData.purchasedRegions.toSet()
            prefs[Keys.PURCHASED_COUNTRIES] = userData.purchasedCountries.toSet()
            prefs[Keys.CURRENT_CITY] = userData.currentCity ?: ""
            if (userData.currentLat != null) prefs[Keys.LAT] = userData.currentLat
            if (userData.currentLng != null) prefs[Keys.LNG] = userData.currentLng
            prefs[Keys.FAVORITES] = userData.favorites.toSet()
            prefs[Keys.VISITED] = userData.visited.toSet()
        }
    }


    suspend fun clearAllUserData() {
        context.dataStore.edit { it.clear() }
    }

    // --- Функции работы с NOTIFICATIONS ---
    suspend fun setNotification(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS] = enabled
        }
    }
    suspend fun getNotification(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.NOTIFICATIONS] != false
    }

    // --- Функции работы с MEASURED ---
    suspend fun saveMeasurement(units: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MEASURED] = units
        }
    }
    suspend fun getMeasurement(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.MEASURED] ?: "km"
    }

    // --- Функции работы с THEME ---
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme
        }
    }
    suspend fun getTheme(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.THEME] ?: "light"
    }

    // --- Функции работы с Levels ---

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

    suspend fun levelUpCategory(category: String, newLevel: Int, rewardPoints: Int) {
        val levels = getCategoryLevels().toMutableMap()
        levels[category] = newLevel
        saveCategoryLevels(levels)
        addGP(rewardPoints)
    }

    // --- Функции работы с GuidePoints ---

    suspend fun getTotalGP(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.TOTAL_GP] ?: 0
    }

    suspend fun getCurrentGP(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.CURRENT_GP] ?: 0
    }

    suspend fun getSpentGP(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.SPENT_GP] ?: 0
    }

    suspend fun addGP(points: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CURRENT_GP] ?: 0
            val total = prefs[Keys.TOTAL_GP] ?: 0
            prefs[Keys.CURRENT_GP] = current + points
            prefs[Keys.TOTAL_GP] = total + points
        }
    }

    suspend fun spentGP(points: Int): Boolean {
        val prefs = context.dataStore.data.first()
        val current = prefs[Keys.CURRENT_GP] ?: 0
        val spent = prefs[Keys.SPENT_GP] ?: 0
        return if (current >= points) {
            context.dataStore.edit { prefsEdit ->
                prefsEdit[Keys.CURRENT_GP] = current - points
                prefsEdit[Keys.SPENT_GP] = spent + points
            }
            true
        } else {
            false
        }
    }

    suspend fun resetGP() {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_GP] = 0
            prefs[Keys.TOTAL_GP] = 0
            prefs[Keys.SPENT_GP] = 0
        }
    }

    // --- Функции работы с LANGUAGE ---

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun getLanguage(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.LANGUAGE] ?: ""
    }

    // --- Функции работы с COLLECTION_REGIONS ---

    suspend fun addRegionInCollection(region: Region) {
        context.dataStore.edit { prefs ->
            // Получаем текущий список регионов из DataStore
            val currentJson = prefs[Keys.COLLECTION_REGIONS]
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
            prefs[Keys.COLLECTION_REGIONS] = Json.encodeToString(updatedList)
        }
    }

    suspend fun getCollectionRegions(): List<Region> {
        val prefs = context.dataStore.data.first()
        val json = prefs[Keys.COLLECTION_REGIONS] ?: return emptyList()
        return Json.decodeFromString(json)
    }

    // --- Функции работы с PURCHASED_REGIONS & PURCHASED_COUNTRIES ---

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

    // --- Функции работы с CURRENT_CITY ---

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

    // --- Функции работы с FAVORITES & VISITED POIs ---

    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.FAVORITES] ?: emptySet() }

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

    val visitedIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.VISITED] ?: emptySet() }

    suspend fun markVisited(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.VISITED] ?: emptySet()
            prefs[Keys.VISITED] = current + id
        }
    }
}