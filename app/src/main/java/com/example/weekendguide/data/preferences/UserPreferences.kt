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

class UserPreferences(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val HOME_REGION = stringPreferencesKey("home_region") // JSON-строка Region
        val PURCHASED_REGIONS = stringSetPreferencesKey("purchased_regions")
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
}
