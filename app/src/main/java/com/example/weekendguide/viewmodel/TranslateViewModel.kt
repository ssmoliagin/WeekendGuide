package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.locales.LocalizerTypes
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.LocalesRepo
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class TranslateViewModel(
    application: Application,
    private val repo: LocalesRepo,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val _lang = MutableStateFlow("")
    val language: StateFlow<String> = _lang.asStateFlow()

    init {
        viewModelScope.launch {
            _lang.value = prefs.getLanguage()
            loadUITranslations()
        }
    }

    fun detectLanguage() {
        viewModelScope.launch {
            val saved = prefs.getLanguage()
            if (saved.isBlank()) {
                val systemLang = Locale.getDefault().language
                val supported = listOf("de", "ru")
                val selected = if (systemLang in supported) systemLang else "en"
                prefs.saveLanguage(selected)
                _lang.value = selected
            } else {
                _lang.value = saved // ✅ если язык есть, обновляем сразу
            }
            loadUITranslations()
        }
    }

    fun setLanguage(newLang: String) {
        viewModelScope.launch {
            prefs.saveLanguage(newLang)
            _lang.value = newLang

            // 🔁 Обновляем также в Firestore
            val currentData = prefs.userDataFlow.first()
            val updatedData = currentData.copy(language = newLang)
            prefs.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    private suspend fun loadUITranslations() {
        val json = repo.downloadTranslationsJson()
        if (json != null) {
            LocalizerUI.loadFromJson(json)
        }
    }

}