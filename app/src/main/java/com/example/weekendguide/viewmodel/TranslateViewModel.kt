package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.LocalesRepo
import com.example.weekendguide.data.repository.UserRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class TranslateViewModelFactory(
    private val localesRepo: LocalesRepo,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TranslateViewModel::class.java) -> {
                TranslateViewModel(localesRepo, userPreferences, userRemote) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}

class TranslateViewModel(
    private val localesRepo: LocalesRepo,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    private val _lang = MutableStateFlow("en")
    val language: StateFlow<String> = _lang.asStateFlow()

    init {
        viewModelScope.launch {
            _lang.value = userPreferences.getLanguage()
            loadUITranslations()
        }
    }

    fun detectLanguage() {
        viewModelScope.launch {
            val savedLang = userPreferences.getLanguage()
            if (savedLang.isBlank()) {
                val systemLang = Locale.getDefault().language
                val supported = listOf("de", "ru")
                val selected = if (systemLang in supported) systemLang else "en"
                userPreferences.saveLanguage(selected)
                _lang.value = selected
            } else {
                _lang.value = savedLang
            }

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(language = _lang.value)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun setLanguage(newLang: String) {
        viewModelScope.launch {
            userPreferences.saveLanguage(newLang)
            _lang.value = newLang

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(language = newLang)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    private suspend fun loadUITranslations() {
        val json = localesRepo.downloadTranslationsJson()
        if (json != null) {
            LocalizerUI.loadFromJson(json)
        }
    }
}
