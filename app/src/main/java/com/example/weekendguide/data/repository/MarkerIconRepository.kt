package com.example.weekendguide.data.repository

import android.graphics.Bitmap

interface MarkerIconRepository {
    suspend fun preload(types: Set<String>)
    fun getCachedBitmap(type: String, isVisited: Boolean, isFavorite: Boolean): Bitmap?
}
