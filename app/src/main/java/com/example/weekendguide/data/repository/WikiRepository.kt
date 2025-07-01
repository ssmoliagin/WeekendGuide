package com.example.weekendguide.data.repository

interface WikiRepository {
    suspend fun fetchWikipediaDescription(title: String, language: String): String?
}