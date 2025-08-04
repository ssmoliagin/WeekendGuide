package com.example.weekendguide.data.repository

import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region

interface DataRepository {
    suspend fun getCountries(): List<Country>
    suspend fun getRegions(countryCode: String): List<Region>
    suspend fun getPOIs(regionCode: String): List<POI>
    suspend fun downloadAndCachePOI(region: Region)
    suspend fun downloadTypesJson(): String?
    suspend fun getTypes(): String?
    suspend fun downloadTagsJson(): String?
    suspend fun getTags(): String?
}
