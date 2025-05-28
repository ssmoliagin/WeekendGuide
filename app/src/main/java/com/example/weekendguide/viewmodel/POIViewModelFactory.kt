package com.example.weekendguide.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.repository.DataRepositoryImpl

class POIViewModelFactory(
    private val context: Context,
    private val region: Region
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(POIViewModel::class.java)) {
            val repository = DataRepositoryImpl(context)
            @Suppress("UNCHECKED_CAST")
            return POIViewModel(repository, region) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
