package com.example.weekendguide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModelFactory(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModel() {

    private val _units = MutableStateFlow("km")
    val units: StateFlow<String> = _units.asStateFlow()

    private val _notification = MutableStateFlow(true)
    val notification: StateFlow<Boolean> = _notification.asStateFlow()

    init {
        viewModelScope.launch {
            _units.value = userPreferences.getMeasurement()
            _notification.value = userPreferences.getNotification()
        }
    }

    // Set user measurement units
    fun setUserMeasurement(newMeas: String) {
        viewModelScope.launch {
            userPreferences.saveMeasurement(newMeas)
            _units.value = newMeas

            // Update in Firestore as well
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(userMeasurement = newMeas)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    // Enable or disable notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotification(enabled)
            _notification.value = enabled

            // Update in Firestore as well
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(notification = enabled)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    // Sign out user
    fun signOut(onFinished: () -> Unit) {
        viewModelScope.launch {
            userPreferences.clearAllUserData()
            FirebaseAuth.getInstance().signOut()
            onFinished()
        }
    }

    // Delete user account
    fun deleteAccount(onResult: (success: Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (user == null || userId == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                userRemote.deleteUserFromFirestore(userId)
                user.delete().await()
                userPreferences.clearAllUserData()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
