package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val country_code: String,
    val region_code: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val default_language: String
   //val previewUrl: String
)
