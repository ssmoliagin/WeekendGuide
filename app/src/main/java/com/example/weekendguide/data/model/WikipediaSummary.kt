package com.example.weekendguide.data.model

data class WikipediaSummary(
    val extract: String?,
    val thumbnail: Thumbnail? = null
)

data class Thumbnail(val source: String)
