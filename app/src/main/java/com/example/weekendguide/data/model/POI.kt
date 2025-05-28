package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable


@Serializable
data class POI(
    val id: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val description: String,
    val type: String,
    val tags: List<String>,
    val imageUrl: String
)
