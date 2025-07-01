package com.example.weekendguide.data.repository

import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.viewmodel.TranslateViewModel

interface LocalesRepo {
    suspend fun downloadTranslationsJson(): String?
}