package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.UserRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SubscriptionViewModelFactory(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(auth, firestore, userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SubscriptionViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    private val _subscriptionBenefitsVisible = MutableStateFlow(false)
    val subscriptionBenefitsVisible: StateFlow<Boolean> = _subscriptionBenefitsVisible.asStateFlow()

    fun toggleSubscriptionBenefitVisibility() {
        _subscriptionBenefitsVisible.value = !_subscriptionBenefitsVisible.value
    }

    fun setSubscriptionEnabled(enabled: Boolean, subToken: String?) {
        viewModelScope.launch {
            userPreferences.setSubscription(enabled)

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(
                subscription = enabled,
                subToken = subToken
            )
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }
}