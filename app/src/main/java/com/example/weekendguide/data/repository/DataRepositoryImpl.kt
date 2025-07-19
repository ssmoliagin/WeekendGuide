package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

class DataRepositoryImpl(private val context: Context) : DataRepository {

    private val path = Constants.FIREBASE_STORAGE_URL
    private val storage = Firebase.storage
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun downloadTypesJson(): String? = withContext(Dispatchers.IO) {
        val url = "$path/data/locales/type.json"
        val file = File(context.cacheDir, "type.json")

        return@withContext try {
            val ref = storage.getReferenceFromUrl(url)
            val metadata = ref.metadata.await()
            val remoteUpdated = metadata.updatedTimeMillis
            val localUpdated = if (file.exists()) file.lastModified() else 0L

            if (remoteUpdated > localUpdated) {
                ref.getFile(file).await()
            } else {
                Log.d("LocalesRepo", "Using cached type.json")
            }
            file.readText()
        } catch (e: Exception) {
            Log.e("LocalesRepo", "Error loading type.json", e)
            null
        }
    }

    override suspend fun getTypes(): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "type.json")
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error reading cached type.json", e)
            null
        }
    }

    override suspend fun getCountries(): List<Country> = withContext(Dispatchers.IO) {
        val url = "$path/data/places/countries.json"
        val file = File(context.cacheDir, "countries.json")
        try {
            val ref = storage.getReferenceFromUrl(url)
            val metadata = ref.metadata.await()
            val remoteUpdated = metadata.updatedTimeMillis
            val localUpdated = if (file.exists()) file.lastModified() else 0L

            if (remoteUpdated > localUpdated) {
                ref.getFile(file).await()
                file.setLastModified(remoteUpdated)
            } else {
                Log.d("DataRepo", "Using cached countries.json")
            }
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Log.e("DataRepo", "Error loading countries.json", e)
            emptyList()
        }
    }

    override suspend fun getRegions(countryCode: String): List<Region> = withContext(Dispatchers.IO) {
        val lowerCode = countryCode.lowercase(Locale.ROOT)
        val url = "$path/data/places/$lowerCode/regions.json"
        val file = File(context.cacheDir, "regions_$lowerCode.json")
        try {
            val ref = storage.getReferenceFromUrl(url)
            val metadata = ref.metadata.await()
            val remoteUpdated = metadata.updatedTimeMillis
            val localUpdated = if (file.exists()) file.lastModified() else 0L

            if (remoteUpdated > localUpdated) {
                ref.getFile(file).await()
                file.setLastModified(remoteUpdated)
            } else {
                Log.d("DataRepo", "Using cached regions.json for $countryCode")
            }

            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Log.e("DataRepo", "Error loading regions.json for $countryCode", e)
            emptyList()
        }
    }

    override suspend fun downloadAndCachePOI(region: Region, translateViewModel: TranslateViewModel) {
        withContext(Dispatchers.IO) {
            try {
                val language = translateViewModel.language.value
                val remotePath = "$path/data/places/${region.country_code}/${region.region_code}/poi/$language.csv"
                val localFile = File(context.cacheDir, "poi_${region.region_code}_$language.csv")
                val ref = storage.getReferenceFromUrl(remotePath)

                val metadata = ref.metadata.await()
                val remoteLastModified = metadata.updatedTimeMillis
                val localLastModified = localFile.lastModified()

                if (!localFile.exists() || remoteLastModified > localLastModified) {
                    ref.getFile(localFile).await()
                    localFile.setLastModified(remoteLastModified)
                } else {
                    Log.d("DataRepo", "Using cached POI file for ${region.region_code}")
                }
            } catch (e: Exception) {
                Log.e("DataRepo", "Error loading POI for ${region.region_code}", e)
            }
        }
    }

    override suspend fun getPOIs(regionCode: String, translateViewModel: TranslateViewModel): List<POI> = withContext(Dispatchers.IO) {

        val language = translateViewModel.language.value

        val file = File(context.cacheDir, "poi_${regionCode}_$language.csv")

        if (!file.exists()) {
            return@withContext emptyList()
        }
        try {
            parseCsvToPoi(file)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseCsvToPoi(file: File): List<POI> {
        return file.readLines()
            .drop(1)
            .mapNotNull { line ->
                val tokens = line.split(";")
                try {
                    POI(
                        id = tokens[0].trim(),
                        lat = tokens[1].trim().toDouble(),
                        lng = tokens[2].trim().toDouble(),
                        title = tokens[3].trim(),
                        description = tokens[4].trim(),
                        type = tokens[5].trim(),
                        tags = tokens[6].split(",").map { it.trim() },
                        imageUrl = tokens.getOrNull(7)?.trim().orEmpty()
                    )
                } catch (e: Exception) {
                    Log.w("DataRepo", "Skipping invalid POI line: $line")
                    null
                }
            }
    }
}
