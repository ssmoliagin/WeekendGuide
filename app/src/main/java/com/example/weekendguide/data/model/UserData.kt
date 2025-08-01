package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class UserData(
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val language: String? = null,
    val userThema: String? = "light",
    val userMeasurement: String? = "km",
    val notification: Boolean? = true,
    val total_GP: Int = 0,
    val current_GP: Int = 0,
    val spent_GP: Int = 0,
    val premium_mode: Boolean? = false,

    val categoryLevels: Map<String, Int> = emptyMap(),
    val collectionRegions: List<Region> = emptyList(),
    val purchasedRegions: List<String> = emptyList(),
    val purchasedCountries: List<String> = emptyList(),
    val currentCity: String? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val favorites: List<String> = emptyList(),
    val visited: List<String> = emptyList(),
)