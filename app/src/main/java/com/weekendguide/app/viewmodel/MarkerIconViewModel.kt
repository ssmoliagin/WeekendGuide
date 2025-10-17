package com.weekendguide.app.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.repository.MarkerIconRepository
import com.weekendguide.app.data.repository.MarkerIconRepositoryImpl
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkerIconViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerIconViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarkerIconViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MarkerIconViewModel(application: Application) : AndroidViewModel(application) {

    private val markerIconRepository: MarkerIconRepository = MarkerIconRepositoryImpl(application.applicationContext)

    private val iconsCache = mutableMapOf<String, BitmapDescriptor>()
    private val bitmapKeys = mutableSetOf<String>()
    private val typesToPreload = setOf(
        "castles", "parks", "city", "interesting", "nature", "museum", "fun", "zoo",
        "water", "active", "hiking", "cycling", "culture"
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                markerIconRepository.preload(typesToPreload)
                typesToPreload.forEach { type ->
                    listOf(true, false).forEach { visited ->
                        listOf(true, false).forEach { favorite ->
                            bitmapKeys.add("$type-$visited-$favorite")
                        }
                    }
                }
            } catch (_: Throwable) { }
        }
    }

    suspend fun prepareDescriptorsIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        try {
            MapsInitializer.initialize(context.applicationContext)
        } catch (_: Throwable) { /* ignore */ }

        bitmapKeys.forEach { key ->
            if (iconsCache.containsKey(key)) return@forEach

            val parts = key.split("-")
            if (parts.size < 3) return@forEach
            val type = parts[0]
            val visited = parts[1].toBoolean()
            val favorite = parts[2].toBoolean()

            val bitmap: Bitmap? = markerIconRepository.getCachedBitmap(type, visited, favorite)

            val descriptor = try {
                if (bitmap != null) {
                    BitmapDescriptorFactory.fromBitmap(bitmap)
                } else {
                    fallbackDescriptorByStatus(visited, favorite)
                }
            } catch (_: Throwable) {
                try { fallbackDescriptorByStatus(visited, favorite) } catch (_: Throwable) {
                    val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    BitmapDescriptorFactory.fromBitmap(bmp)
                }
            }

            iconsCache[key] = descriptor
        }
    }

    fun getDescriptorSync(type: String, isVisited: Boolean, isFavorite: Boolean): BitmapDescriptor? {
        val key = "$type-$isVisited-$isFavorite"
        return iconsCache[key]
    }

    private fun fallbackDescriptorByStatus(isVisited: Boolean, isFavorite: Boolean): BitmapDescriptor {
        return try {
            when {
                isVisited -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                isFavorite -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            }
        } catch (_: Throwable) {
            try {
                val color = when {
                    isVisited -> android.graphics.Color.GREEN
                    isFavorite -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.RED
                }
                val bmp = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(color)
                BitmapDescriptorFactory.fromBitmap(bmp)
            } catch (_: Throwable) {
                null as BitmapDescriptor
            }
        }
    }
}
