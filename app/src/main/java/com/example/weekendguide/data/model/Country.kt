package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Country(
    val countryCode: String,
    val name_en: String,
    val name_de: String,
    val name_ru: String
)
