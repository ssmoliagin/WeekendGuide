package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepository
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.data.repository.WikiRepositoryImp

class POIViewModelFactory(
    private val region: List<Region>,
    private val translateViewModel: TranslateViewModel,
    private val dataRepository: DataRepository,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(POIViewModel::class.java)) {
            val wikiRepository = WikiRepositoryImp()
            return POIViewModel(
                translateViewModel,
                dataRepository,
                wikiRepository,
                region,
                userPreferences,
                userRemote
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
