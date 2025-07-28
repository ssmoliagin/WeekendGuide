package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class POI(
    val id_country: String,
    val id_region: String,
    val id: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val description: String,
    val title_en: String,
    val description_en: String,
    val title_de: String,
    val description_de: String,
    val title_ru: String,
    val description_ru: String,
    val type: String,
    val tags: List<String>,
    val imageUrl: String
)
