package com.example.weekendguide.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModelFactory(
    private val app: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(app, userPreferences, userRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(
    private val app: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(app) {

    private val _units = MutableStateFlow("km")
    private val _notification = MutableStateFlow(true)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun setUserMeasurement(newMeas: String) {
        viewModelScope.launch {
            userPreferences.saveMeasurement(newMeas)
            _units.value = newMeas

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(userMeasurement = newMeas)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotification(enabled)
            _notification.value = enabled

            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(notification = enabled)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userPreferences.clearAllUserData()
            FirebaseAuth.getInstance().signOut()

            app.cacheDir.deleteRecursively()
            app.filesDir.deleteRecursively()

            val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        }
    }

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

                app.cacheDir.deleteRecursively()
                app.filesDir.deleteRecursively()

                val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                app.startActivity(intent)

                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            val currentData = userPreferences.userDataFlow.first()
            val updated = currentData.copy(displayName = name)
            userPreferences.saveUserData(updated)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    fun updateUserEmail(currentPassword: String, newEmail: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val currentEmail = user.email ?: return@launch

                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)

                // Reauthenticate
                user.reauthenticate(credential).await()

                // Verify before update
                user.verifyBeforeUpdateEmail(newEmail).await()

                val currentData = userPreferences.userDataFlow.first()
                val updated = currentData.copy(email = newEmail)
                userPreferences.saveUserData(updated)
                userRemote.launchSyncLocalToRemote(viewModelScope)

                _toastMessage.value = "check_email_verification"

            } catch (e: Exception) {
                _toastMessage.value = "email_update_error"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
