package com.example.weekendguide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val SELECTED_REGION = stringPreferencesKey("selected_region")
        val HOME_REGION = stringPreferencesKey("home_region")
        val UNLOCKED_REGIONS = stringSetPreferencesKey("unlocked_regions")
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language }
    }

    suspend fun getLanguage(): String? {
        return context.dataStore.data.map { it[Keys.LANGUAGE] }.first()
    }

    suspend fun saveSelectedRegion(regionId: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SELECTED_REGION] = regionId }
    }

    suspend fun getSelectedRegion(): String? {
        return context.dataStore.data.map { it[Keys.SELECTED_REGION] }.first()
    }

    suspend fun saveHomeRegion(regionId: String) {
        context.dataStore.edit { prefs -> prefs[Keys.HOME_REGION] = regionId }
    }

    suspend fun getHomeRegion(): String? {
        return context.dataStore.data.map { it[Keys.HOME_REGION] }.first()
    }

    suspend fun addUnlockedRegion(regionId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_REGIONS]?.toMutableSet() ?: mutableSetOf()
            current.add(regionId)
            prefs[Keys.UNLOCKED_REGIONS] = current
        }
    }

    suspend fun getUnlockedRegions(): Set<String> {
        return context.dataStore.data.map { it[Keys.UNLOCKED_REGIONS] ?: emptySet() }.first()
    }

    suspend fun isRegionUnlocked(regionId: String): Boolean {
        return getUnlockedRegions().contains(regionId)
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
