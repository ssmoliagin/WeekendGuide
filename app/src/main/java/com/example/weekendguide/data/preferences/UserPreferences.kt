package com.example.weekendguide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.weekendguide.data.model.Region
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val language: String,
    val homeRegion: Region?,
    val purchasedRegions: Set<String>,
    val currentCity: String?,
    val currentLocation: Pair<Double, Double>?
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val HOME_REGION = stringPreferencesKey("home_region") // JSON-строка Region
        val PURCHASED_REGIONS = stringSetPreferencesKey("purchased_regions") // ПОКА НЕЯСНО ЗАЧЕМ
        val CURRENT_CITY = stringPreferencesKey("current_city")
        val LAT = doublePreferencesKey("current_lat")
        val LNG = doublePreferencesKey("current_lng")
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun getLanguage(): String {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.LANGUAGE] ?: "de"
    }

    suspend fun saveHomeRegion(region: Region) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HOME_REGION] = Json.encodeToString(region)
        }
    }

    suspend fun getHomeRegion(): Region? {
        val prefs = context.dataStore.data.first()
        val json = prefs[Keys.HOME_REGION] ?: return null
        return Json.decodeFromString<Region>(json)
    }

    suspend fun addPurchasedRegion(regionCode: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PURCHASED_REGIONS] ?: emptySet()
            prefs[Keys.PURCHASED_REGIONS] = current + regionCode
        }
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

    suspend fun getAll(): UserSettings {
        val prefs = context.dataStore.data.first()

        val language = prefs[Keys.LANGUAGE] ?: "de"
        val homeRegionJson = prefs[Keys.HOME_REGION]
        val homeRegion = homeRegionJson?.let { Json.decodeFromString<Region>(it) }
        val purchasedRegions = prefs[Keys.PURCHASED_REGIONS] ?: emptySet()
        val currentCity = prefs[Keys.CURRENT_CITY]
        val lat = prefs[Keys.LAT]
        val lng = prefs[Keys.LNG]
        val currentLocation = if (lat != null && lng != null) Pair(lat, lng) else null

        return UserSettings(
            language = language,
            homeRegion = homeRegion,
            purchasedRegions = purchasedRegions,
            currentCity = currentCity,
            currentLocation = currentLocation
        )
    }


}
