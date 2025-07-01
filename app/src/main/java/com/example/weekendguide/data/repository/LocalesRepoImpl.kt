package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.Country
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class LocalesRepoImpl(private val context: Context) : LocalesRepo {

    override suspend fun downloadTranslationsJson(): String? = withContext(Dispatchers.IO) {
        try {
            context.assets.open("locales/ui.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("LocalesRepo", "Error loading ui.json from assets", e)
            null
        }
    }

    /* // загрузка из Фаербазы
    private val path = Constants.FIREBASE_STORAGE_URL
    private val storage = Firebase.storage
    override suspend fun downloadTranslationsJson(): String? = withContext(Dispatchers.IO) {
        val url = "$path/data/locales/ui.json"
        val file = File(context.cacheDir, "ui.json")
        return@withContext try {
            if (!file.exists()) {
                Log.d("LocalesRepo", "Downloading ui.json")
                val ref = storage.getReferenceFromUrl(url)
                ref.getFile(file).await()
            } else {
                Log.d("LocalesRepo", "Using cached ui.json")
            }
            file.readText()
        } catch (e: Exception) {
            Log.e("LocalesRepo", "Error loading ui.json", e)
            null
        }
    }

     */
}