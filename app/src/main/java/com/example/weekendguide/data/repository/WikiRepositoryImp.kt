package com.example.weekendguide.data.repository

import android.content.Context
import com.example.weekendguide.data.model.WikipediaSummary
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language

class WikiRepositoryImp(private val context: Context) : WikiRepository {

    override suspend fun fetchWikipediaDescription(title: String, language: String): String? {
        val url = "https://${language}.wikipedia.org/api/rest_v1/page/summary/$title"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val summary = Gson().fromJson(json, WikipediaSummary::class.java)
                    summary.extract
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}