package com.example.weekendguide.data.repository

import android.content.Context
import android.util.Log
import com.example.weekendguide.data.model.WikipediaSummary
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Protocol

class WikiRepositoryImp(private val context: Context) : WikiRepository {

    override suspend fun fetchWikipediaDescription(title: String, language: String): String? {
        val encodedTitle = title.replace(" ", "%20")
        val url = "https://$language.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

        Log.d("WikiRepository", "Wikipedia request URL: $url")

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1)) // <-- ключевая строка
            .build()

        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val summary = Gson().fromJson(json, WikipediaSummary::class.java)
                    summary.extract
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