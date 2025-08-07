package com.example.weekendguide.data.repository

import android.util.Log
import com.example.weekendguide.data.model.WikipediaSummary
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request

class WikiRepositoryImp() : WikiRepository {

    override suspend fun fetchWikipediaDescription(title: String, language: String): String? {
        val encodedTitle = title.replace(" ", "%20")
        val url = "https://$language.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val summary = Gson().fromJson(json, WikipediaSummary::class.java)
                    val extract = summary.extract ?: return@withContext null
                    val articleUrl = "https://$language.wikipedia.org/wiki/$encodedTitle"
                    "$extract\n\n[Wikipedia]($articleUrl)"
                } else {
                    Log.e("WikiRepository", "Wikipedia API error ${response.code}: ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e("WikiRepository", "Error fetching Wikipedia description", e)
                null
            }
        }
    }

}