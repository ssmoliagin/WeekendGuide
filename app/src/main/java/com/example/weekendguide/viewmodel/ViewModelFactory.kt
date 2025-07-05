package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.repository.LocalesRepo

class ViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> {
                LocationViewModel(app) as T
            }
            modelClass.isAssignableFrom(PointsViewModel::class.java) -> {
                PointsViewModel(app) as T
            }
            modelClass.isAssignableFrom(ThemeViewModel::class.java) -> {
                ThemeViewModel(app) as T
            }
            modelClass.isAssignableFrom(MainStateViewModel::class.java) -> {
                MainStateViewModel(app) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}