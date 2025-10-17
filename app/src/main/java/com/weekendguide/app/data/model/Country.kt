package com.weekendguide.app.data.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Country(
    val countryCode: String,
    val name_en: String,
    val name_de: String,
    val name_ru: String
)
