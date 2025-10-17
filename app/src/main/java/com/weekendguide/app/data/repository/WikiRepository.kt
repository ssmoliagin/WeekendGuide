package com.weekendguide.app.data.repository

import android.util.Log
import com.weekendguide.app.BuildConfig
import com.weekendguide.app.Constants.APP_DOCS_URL
import com.weekendguide.app.Constants.CONTACT_EMAIL
import com.weekendguide.app.data.model.WikipediaSummary
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request

interface WikiRepository {
    suspend fun fetchWikipediaDescription(title: String, language: String): String?
}

class WikiRepositoryImp : WikiRepository {

    override suspend fun fetchWikipediaDescription(title: String, language: String): String? {
        val encodedTitle = title.replace(" ", "_")
        val url = "https://$language.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val request = Request.Builder()
            .url(url)
            // ⚡️ User-Agent
            .header(
                "User-Agent",
                "WeekendGuideApp/${BuildConfig.VERSION_NAME} ($APP_DOCS_URL; $CONTACT_EMAIL)"
            )
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val summary = Gson().fromJson(json, WikipediaSummary::class.java)
                    val extract = summary.extract ?: return@withContext null
                    val safeTitleForUrl = title.replace("(", "%28").replace(")", "%29").replace(" ", "_")
                    val articleUrl = "https://$language.wikipedia.org/wiki/$safeTitleForUrl"
                    "$extract\n\n[Wikipedia]($articleUrl)"
                } else {
                    Log.e("WikiRepository", "Wikipedia API error $url ${response.code}: ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e("WikiRepository", "Error fetching Wikipedia description", e)
                null
            }
        }
    }
}