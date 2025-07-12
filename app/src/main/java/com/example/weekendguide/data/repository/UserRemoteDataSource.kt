package com.example.weekendguide.data.repository

import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
) {

    private val usersCollection = firestore.collection("users")

    suspend fun syncOnLogin(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val userId = user.uid

        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                // Пользователь есть на сервере — загрузить данные в локальное хранилище
                val remoteUserData = doc.toObject(UserData::class.java)
                if (remoteUserData != null) {
                    userPreferences.saveUserData(remoteUserData)
                }
            } else {
                // Пользователя нет — создать запись на сервере с базовыми данными
                val newUserData = UserData(
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    language = userPreferences.getLanguage() //автоопределился при старте
                )
                usersCollection.document(userId).set(newUserData).await()
                userPreferences.saveUserData(newUserData)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Вызывать при изменении настроек локально,
     * чтобы сразу синхронизировать с Firestore.
     */
    fun launchSyncLocalToRemote(scope: CoroutineScope) {
        scope.launch {
            try {
                syncLocalToRemote()
            } catch (e: Exception) {

            }
        }
    }

    suspend fun syncLocalToRemote() {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        val userId = user.uid
        val localData = userPreferences.userDataFlow.first()
        usersCollection.document(userId).set(localData).await()
    }

    suspend fun deleteUserFromFirestore(userId: String) {
        usersCollection.document(userId).delete().await()
    }
}
