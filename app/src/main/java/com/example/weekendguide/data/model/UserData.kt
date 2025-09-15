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
    val homeCity: String? = null,
    val currentCity: String? = null,
    val fcm_token: String? = null,
    val app_version: String? = null,

    // --- Booleans ---
    val notification: Boolean? = true,
    val subscription: Boolean? = false,
    val test_mode: Boolean? = false,

    // --- Integers ---
    val total_GP: Int = 0,
    val current_GP: Int = 0,
    val spent_GP: Int = 0,

    // --- Doubles ---
    val homeLat: Double? = null,
    val homeLng: Double? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null,

    // --- Collections ---
    val categoryLevels: Map<String, Int> = emptyMap(),
    val collectionRegions: List<Region> = emptyList(),
    val subscriptionRegions: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val visited: Map<String, Boolean> = emptyMap(),
    val rewardAvailable: Map<String, Boolean> = emptyMap(),
)