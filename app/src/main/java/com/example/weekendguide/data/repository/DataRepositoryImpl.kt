package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

class DataRepositoryImpl(private val context: Context) : DataRepository {

    private val path = "gs://weekendguide-dfc8c.firebasestorage.app"//BuildConfig.FIREBASE_STORAGE_PATH
    private val storage = Firebase.storage
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCountries(): List<Country> = withContext(Dispatchers.IO) {
        val url = "$path/data/places/countries.json"
        val file = File(context.cacheDir, "countries.json")
        try {
            if (!file.exists()) {
                Log.d("DataRepo", "Downloading countries.json")
                val ref = storage.getReferenceFromUrl(url)
                ref.getFile(file).await()
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
        val userPreferences = UserPreferences(context)

      //  userPreferences.saveSelectCountry(lowerCode) // неработает!!!
      //  Log.d("УСТАНОВЛЕН КОД СТРАНЫ", "${userPreferences.getSelectCountry()}")

        val url = "$path/data/places/$lowerCode/regions.json"
        val file = File(context.cacheDir, "regions_$lowerCode.json")
        try {
            if (!file.exists()) {
                Log.d("DataRepo", "Downloading regions.json for $countryCode")
                val ref = storage.getReferenceFromUrl(url)
                ref.getFile(file).await()
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

    override suspend fun downloadAndCachePOI(region: Region) {
        withContext(Dispatchers.IO) {
            try {
                val userPreferences = UserPreferences(context)
                val language = userPreferences.getLanguage()

                val remotePath = "$path/data/places/${region.country_code}/${region.region_code}/poi/$language.csv"
                val localFile = File(context.cacheDir, "poi_${region.region_code}_$language.csv")

                val ref = storage.getReferenceFromUrl(remotePath)
                ref.getFile(localFile).await()
            } catch (e: Exception) {
                Log.e("DataRepo", "Ошибка загрузки POI для региона ${region.region_code}", e)
            }
        }
    }

    override suspend fun getPOIs(regionCode: String): List<POI> = withContext(Dispatchers.IO) {
        val userPreferences = UserPreferences(context)
        val language = userPreferences.getLanguage()
       // val language = Locale.getDefault().language
        val file = File(context.cacheDir, "poi_${regionCode}_$language.csv")

        if (!file.exists()) {
            Log.w("DataRepo", "POI file not found: ${file.absolutePath}")
            return@withContext emptyList()
        }

        try {
            Log.d("DataRepo", "Parsing POI file: ${file.absolutePath}")
            parseCsvToPoi(file)
        } catch (e: Exception) {
            Log.e("DataRepo", "Error parsing POI CSV for $regionCode", e)
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
