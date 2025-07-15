package com.example.weekendguide.data.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class Review(
    val poiId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val rating: Int = 0,
    val text: String = "",
    val timestamp: Long = 0L
)