package com.weekendguide.app.data.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Region(
    val country_code: String = "",
    val region_code: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val default_language: String = "",
)
