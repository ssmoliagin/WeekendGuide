package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface LocalesRepo {
    suspend fun downloadTranslationsJson(): String?
}

class LocalesRepoImpl(private val context: Context) : LocalesRepo {

    override suspend fun downloadTranslationsJson(): String? = withContext(Dispatchers.IO) {
        try {
            context.assets.open("locales/ui.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("LocalesRepo", "Error loading ui.json from assets", e)
            null
        }
    }
}