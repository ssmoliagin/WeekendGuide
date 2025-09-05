package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import java.net.URL
import java.security.MessageDigest

interface DataRepository {
    suspend fun getCountries(): List<Country>
    suspend fun getRegions(countryCode: String): List<Region>
    suspend fun getPOIs(regionCode: String): List<POI>
    suspend fun downloadAndCachePOI(region: Region)
    suspend fun downloadTypesJson(): String?
    suspend fun getTypes(): String?
    suspend fun downloadTagsJson(): String?
    suspend fun getTags(): String?
}

class DataRepositoryImpl(private val context: Context) : DataRepository {

    private val path = Constants.FIREBASE_STORAGE_URL
    private val storage = Firebase.storage
    private val json = Json { ignoreUnknownKeys = true }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //type.json
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

    //tags.json
    override suspend fun downloadTagsJson(): String? = withContext(Dispatchers.IO) {
        val url = "$path/data/locales/tags.json"
        val file = File(context.cacheDir, "tags.json")

        return@withContext try {
            val ref = storage.getReferenceFromUrl(url)
            val metadata = ref.metadata.await()
            val remoteUpdated = metadata.updatedTimeMillis
            val localUpdated = if (file.exists()) file.lastModified() else 0L

            if (remoteUpdated > localUpdated) {
                ref.getFile(file).await()
            } else {
                Log.d("LocalesRepo", "Using cached tags.json")
            }
            file.readText()
        } catch (e: Exception) {
            Log.e("LocalesRepo", "Error loading tags.json", e)
            null
        }
    }

    override suspend fun getTags(): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "tags.json")
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error reading cached tags.json", e)
            null
        }
    }

    //countries.json
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

    //region.json
    override suspend fun getRegions(countryCode: String): List<Region> = withContext(Dispatchers.IO) {
        val lowerCode = countryCode.lowercase(Locale.ROOT)
        val url = "$path/data/places/$lowerCode/region.json"
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
                Log.d("DataRepo", "Using cached region.json for $countryCode")
            }

            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Log.e("DataRepo", "Error loading region.json for $countryCode", e)
            emptyList()
        }
    }

    // -------- CSV (POI) --------
    override suspend fun downloadAndCachePOI(region: Region) {
        withContext(Dispatchers.IO) {
            try {
                val remotePath = "$path/data/places/${region.country_code}/poi/${region.region_code}.csv"
                val localFile = File(context.filesDir, "poi_${region.region_code}.csv")
                val ref = storage.getReferenceFromUrl(remotePath)

                val metadata = ref.metadata.await()
                val remoteLastModified = metadata.updatedTimeMillis
                val localLastModified = localFile.lastModified()

                if (!localFile.exists() || remoteLastModified > localLastModified) {
                    ref.getFile(localFile).await()
                    localFile.setLastModified(remoteLastModified)
                    Log.d("DataRepo", "CSV сохранён: ${localFile.absolutePath}")
                }

                // парсим CSV
                val pois = parseCsvToPoi(localFile)

                // проверяем и обновляем локальные пути
                pois.forEach { poi ->
                    val cachedFile = File(context.filesDir, "${poi.id}.jpg")
                    if (cachedFile.exists()) {
                        poi.imageUrl = cachedFile.absolutePath
                    }
                }

                // 🚀 фоновые загрузки картинок
                repositoryScope.launch {
                    pois.forEach { poi ->
                        val localImage = downloadAndCacheImage(context, poi.id, poi.imageUrl)
                        if (localImage != null) {
                            poi.imageUrl = localImage
                            Log.d("ImageCache", "Картинка для ${poi.id} сохранена: ${File(localImage).length()} байт")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("DataRepo", "Error loading POI for ${region.region_code}", e)
            }
        }
    }

    override suspend fun getPOIs(regionCode: String): List<POI> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "poi_${regionCode}.csv")
        if (!file.exists()) return@withContext emptyList()

        try {
            val pois = parseCsvToPoi(file)
            pois.forEach { poi ->
                val cachedFile = File(context.filesDir, "${poi.id}.jpg")
                if (cachedFile.exists()) {
                    poi.imageUrl = cachedFile.absolutePath
                }
            }
            pois
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -------- Скачивание картинок --------
    private suspend fun downloadAndCacheImage(
        context: Context,
        poiId: String,
        imageUrl: String?
    ): String? = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) return@withContext null

        try {
            val localFile = File(context.filesDir, "$poiId.jpg")
            if (!localFile.exists() || localFile.length() == 0L) {
                URL(imageUrl).openStream().use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("ImageCache", "Скачано: $imageUrl (${localFile.length()} байт)")
            } else {
                Log.d("ImageCache", "Используется кэш: $imageUrl (${localFile.length()} байт)")
            }
            localFile.absolutePath
        } catch (e: Exception) {
            Log.e("ImageCache", "Ошибка скачивания $imageUrl")
            null
        }
    }

    private fun parseCsvToPoi(file: File): List<POI> {
        return file.readLines()
            .drop(1)
            .mapNotNull { line ->
                val tokens = line.split(",")
                try {
                    POI(
                        id_country = tokens[0].trim(),
                        id_region = tokens[1].trim(),
                        id = tokens[2].trim(),
                        lat = tokens[3].trim().toDouble(),
                        lng = tokens[4].trim().toDouble(),
                        title = tokens[5].trim(),
                        description = tokens[6].trim(),
                        title_en = tokens[7].trim(),
                        description_en = tokens[8].trim(),
                        title_de = tokens[9].trim(),
                        description_de = tokens[10].trim(),
                        title_ru = tokens[11].trim(),
                        description_ru = tokens[12].trim(),
                        type = tokens[13].trim(),
                        tags = tokens[14].split(" ").map { it.trim() },
                        imageUrl = tokens.getOrNull(15)?.trim().orEmpty()
                    )
                } catch (e: Exception) {
                    Log.w("DataRepo", "Skipping invalid POI line: $line")
                    null
                }
            }
    }
}
