package com.example.weekendguide.data.locales

import org.json.JSONObject

object LocalizerUI {
    private val translations = mutableMapOf<String, Map<String, String>>()

    fun loadFromJson(json: String) {
        val parsed = JSONObject(json)
        parsed.keys().forEach { key ->
            val item = parsed.getJSONObject(key)
            val map = mutableMapOf<String, String>()
            item.keys().forEach { lang ->
                map[lang] = item.getString(lang)
            }
            translations[key] = map
        }
    }

    fun t(key: String, lang: String): String {
        return translations[key]?.get(lang)
            ?: translations[key]?.get("en")
            ?: "[$key]"
    }
}
