package com.example.weekendguide.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.LocalesRepo
import com.example.weekendguide.data.repository.UserRemoteDataSource

class TranslateViewModelFactory(
    private val app: Application,
    private val localesRepo: LocalesRepo,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TranslateViewModel::class.java) -> {
                TranslateViewModel(app, localesRepo, userRemote) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}