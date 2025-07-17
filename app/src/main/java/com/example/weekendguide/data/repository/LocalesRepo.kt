package com.example.weekendguide.data.repository

interface LocalesRepo {
    suspend fun downloadTranslationsJson(): String?
}