package com.example.weekendguide.data.repository

import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.viewmodel.TranslateViewModel

interface DataRepository {
    suspend fun getCountries(): List<Country>
    suspend fun getRegions(countryCode: String): List<Region>
    suspend fun getPOIs(regionCode: String, translateViewModel: TranslateViewModel): List<POI>
    suspend fun downloadAndCachePOI(region: Region, translateViewModel: TranslateViewModel)
    suspend fun downloadTypesJson(): String?
    suspend fun getTypes(): String?
}
