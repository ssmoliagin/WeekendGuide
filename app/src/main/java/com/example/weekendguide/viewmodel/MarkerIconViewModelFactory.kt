package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MarkerIconViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerIconViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarkerIconViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}