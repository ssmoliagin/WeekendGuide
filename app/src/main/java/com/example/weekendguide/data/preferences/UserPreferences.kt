package com.example.weekendguide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    // Preference Keys
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
        val PREMIUM_MODE = booleanPreferencesKey("premium_mode")

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

    // Main user data flow
    val userDataFlow: Flow<UserData> = context.dataStore.data.map { prefs ->
        val levelsJson = prefs[Keys.CATEGORY_LEVELS]
        val collectionJson = prefs[Keys.COLLECTION_REGIONS]

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
            premium_mode = prefs[Keys.PREMIUM_MODE],
            categoryLevels = levelsJson?.let { Json.decodeFromString(it) } ?: emptyMap(),
            collectionRegions = collectionJson?.let { Json.decodeFromString(it) } ?: emptyList(),
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
            prefs[Keys.PREMIUM_MODE] = userData.premium_mode ?: false
            prefs[Keys.CATEGORY_LEVELS] = Json.encodeToString(userData.categoryLevels)
            prefs[Keys.COLLECTION_REGIONS] = Json.encodeToString(userData.collectionRegions)
            prefs[Keys.PURCHASED_REGIONS] = userData.purchasedRegions.toSet()
            prefs[Keys.PURCHASED_COUNTRIES] = userData.purchasedCountries.toSet()
            prefs[Keys.CURRENT_CITY] = userData.currentCity ?: ""
            userData.currentLat?.let { prefs[Keys.LAT] = it }
            userData.currentLng?.let { prefs[Keys.LNG] = it }
            prefs[Keys.FAVORITES] = userData.favorites.toSet()
            prefs[Keys.VISITED] = userData.visited.toSet()
        }
    }

    suspend fun clearAllUserData() {
        context.dataStore.edit { it.clear() }
    }

    // Premium settings
    suspend fun setPremium(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PREMIUM_MODE] = enabled }
    }

    suspend fun getPremium(): Boolean {
        return context.dataStore.data.first()[Keys.PREMIUM_MODE] != false
    }

    // Notification settings
    suspend fun setNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun getNotification(): Boolean {
        return context.dataStore.data.first()[Keys.NOTIFICATIONS] != false
    }

    // Theme settings
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    suspend fun getTheme(): String {
        return context.dataStore.data.first()[Keys.THEME] ?: "light"
    }

    // Measurement unit settings
    suspend fun saveMeasurement(units: String) {
        context.dataStore.edit { it[Keys.MEASURED] = units }
    }

    suspend fun getMeasurement(): String {
        return context.dataStore.data.first()[Keys.MEASURED] ?: "km"
    }

    // Language settings
    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun getLanguage(): String {
        return context.dataStore.data.first()[Keys.LANGUAGE] ?: ""
    }

    // Guide points
    suspend fun getTotalGP(): Int = context.dataStore.data.first()[Keys.TOTAL_GP] ?: 0
    suspend fun getCurrentGP(): Int = context.dataStore.data.first()[Keys.CURRENT_GP] ?: 0
    suspend fun getSpentGP(): Int = context.dataStore.data.first()[Keys.SPENT_GP] ?: 0

    suspend fun addGP(points: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_GP] = (prefs[Keys.CURRENT_GP] ?: 0) + points
            prefs[Keys.TOTAL_GP] = (prefs[Keys.TOTAL_GP] ?: 0) + points
        }
    }

    suspend fun spentGP(points: Int): Boolean {
        val prefs = context.dataStore.data.first()
        val current = prefs[Keys.CURRENT_GP] ?: 0
        val spent = prefs[Keys.SPENT_GP] ?: 0
        return if (current >= points) {
            context.dataStore.edit {
                it[Keys.CURRENT_GP] = current - points
                it[Keys.SPENT_GP] = spent + points
            }
            true
        } else false
    }

    suspend fun resetGP() {
        context.dataStore.edit {
            it[Keys.CURRENT_GP] = 0
            it[Keys.TOTAL_GP] = 0
            it[Keys.SPENT_GP] = 0
        }
    }

    // Category level handling
    suspend fun getCategoryLevels(): Map<String, Int> {
        val json = context.dataStore.data.first()[Keys.CATEGORY_LEVELS] ?: return emptyMap()
        return Json.decodeFromString(json)
    }

    suspend fun saveCategoryLevels(map: Map<String, Int>) {
        context.dataStore.edit {
            it[Keys.CATEGORY_LEVELS] = Json.encodeToString(map)
        }
    }

    suspend fun levelUpCategory(category: String, newLevel: Int) {
        val levels = getCategoryLevels().toMutableMap()
        levels[category] = newLevel
        saveCategoryLevels(levels)
    }

    // Region collection
    suspend fun addRegionInCollection(region: Region) {
        context.dataStore.edit { prefs ->
            val currentList = prefs[Keys.COLLECTION_REGIONS]?.let {
                Json.decodeFromString<List<Region>>(it)
            } ?: emptyList()
            val updatedList = if (region !in currentList) currentList + region else currentList
            prefs[Keys.COLLECTION_REGIONS] = Json.encodeToString(updatedList)
        }
    }

    suspend fun getCollectionRegions(): List<Region> {
        val json = context.dataStore.data.first()[Keys.COLLECTION_REGIONS] ?: return emptyList()
        return Json.decodeFromString(json)
    }

    // Purchased regions/countries
    suspend fun addPurchasedRegion(regionCode: String) {
        context.dataStore.edit {
            val current = it[Keys.PURCHASED_REGIONS] ?: emptySet()
            it[Keys.PURCHASED_REGIONS] = current + regionCode
        }
    }

    suspend fun addPurchasedCountries(countryCode: String) {
        context.dataStore.edit {
            val current = it[Keys.PURCHASED_COUNTRIES] ?: emptySet()
            it[Keys.PURCHASED_COUNTRIES] = current + countryCode
        }
    }

    suspend fun getPurchasedRegions(): Set<String> {
        return context.dataStore.data.first()[Keys.PURCHASED_REGIONS] ?: emptySet()
    }

    suspend fun getPurchasedCountries(): Set<String> {
        return context.dataStore.data.first()[Keys.PURCHASED_COUNTRIES] ?: emptySet()
    }

    // Current city & location
    suspend fun saveCurrentCity(city: String) {
        context.dataStore.edit { it[Keys.CURRENT_CITY] = city }
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

    // Favorites
    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[Keys.FAVORITES] ?: emptySet() }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit {
            val current = it[Keys.FAVORITES] ?: emptySet()
            it[Keys.FAVORITES] = if (id in current) current - id else current + id
        }
    }

    // Visited POIs
    val visitedIdsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[Keys.VISITED] ?: emptySet() }

    suspend fun markVisited(id: String) {
        context.dataStore.edit {
            val current = it[Keys.VISITED] ?: emptySet()
            it[Keys.VISITED] = current + id
        }
    }
}
