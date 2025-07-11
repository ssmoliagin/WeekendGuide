package com.example.weekendguide.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
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
import kotlin.system.exitProcess

class ProfileViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource
) : AndroidViewModel(application) {

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

    //единицы измерения
    fun setUserMeasurement(newMeas: String) {
        viewModelScope.launch {
            userPreferences.saveMeasurement(newMeas)
            _units.value = newMeas

            // 🔁 Обновляем также в Firestore
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(userMeasurement = newMeas)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    //уведомления вкл/выкл
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotification(enabled)
            _notification.value = enabled

            // 🔁 Обновляем также в Firestore
            val currentData = userPreferences.userDataFlow.first()
            val updatedData = currentData.copy(notification = enabled)
            userPreferences.saveUserData(updatedData)
            userRemote.launchSyncLocalToRemote(viewModelScope)
        }
    }

    //выход из аккаунта
    fun signOut(onFinished: () -> Unit) {
        viewModelScope.launch {
            // Очистка кэша и локальных данных
            userPreferences.clearAllUserData()
            FirebaseAuth.getInstance().signOut()
            onFinished()
        }
    }

    //удаление акк
    fun deleteAccount(onResult: (success: Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (user == null || userId == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                // Удалить Firestore-документ
                userRemote.deleteUserFromFirestore(userId)

                // Удалить пользователя в Firebase Auth
                user.delete().await()

                // Очистить локальные данные
                userPreferences.clearAllUserData()

                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        context.startActivity(mainIntent)
        exitProcess(0)
    }
}