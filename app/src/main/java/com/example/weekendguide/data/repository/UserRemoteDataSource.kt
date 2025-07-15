package com.example.weekendguide.data.repository

import android.util.Log
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val reviewsCollection = firestore.collection("reviews")

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

    //ОТЗЫВЫ
    suspend fun submitReview(review: Review) {
        try {
            reviewsCollection
                .add(review)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getReviewsForPoi(poiId: String): List<Review> {
        return try {
            val snapshot = firestore
                .collection("reviews")
                .whereEqualTo("poiId", poiId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("FIRESTORE", "Нет отзывов для poiId: $poiId")
                emptyList()
            } else {
                val reviews = snapshot.toObjects(Review::class.java)
                Log.d("FIRESTORE", "Найдено отзывов: ${reviews.size}")
                reviews
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE", "Ошибка при получении отзывов: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    suspend fun getAllReviews(): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("FIRESTORE - getAllReviews", "Нет отзывов в коллекции reviews")
                emptyList()
            } else {
                val reviews = snapshot.toObjects(Review::class.java)
                Log.d("FIRESTORE - getAllReviews", "Найдено отзывов: ${reviews.size}")
                reviews
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE - getAllReviews", "Ошибка при получении всех отзывов: ${e.localizedMessage}", e)
            emptyList()
        }
    }

}
