package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.model.UserData
import com.weekendguide.app.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.weekendguide.app.service.BillingManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashViewModelFactory(
    private val userPreferences: UserPreferences,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val billingManager: BillingManager,
    ): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(userPreferences, subscriptionViewModel, billingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SplashViewModel(
    private val userPreferences: UserPreferences,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val billingManager: BillingManager,
) : ViewModel() {

    val userData: StateFlow<UserData> = userPreferences.userDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserData()
    )

    private val _uiState = MutableStateFlow<Destination>(Destination.Loading)
    val uiState: StateFlow<Destination> = _uiState

    sealed class Destination {
        object Login : Destination()
        object Store : Destination()
        object Main : Destination()
        object Loading : Destination()
    }

    init {
        viewModelScope.launch {
            delay(1500)
            decideNextScreen()
        }
    }

    private suspend fun decideNextScreen() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _uiState.value = Destination.Login
        } else {

            val savedToken = userPreferences.userDataFlow.first().subToken
            if (!savedToken.isNullOrEmpty()) {
                billingManager.validateSavedSubscriptionToken(savedToken) { isActive ->
                    subscriptionViewModel.setSubscriptionEnabled(isActive, if (isActive) savedToken else null)
                }
            }
            delay(100)
            val regions = userPreferences.getCollectionRegions()
            _uiState.value = if (regions.isNotEmpty()) Destination.Main else Destination.Store
        }
    }
}
