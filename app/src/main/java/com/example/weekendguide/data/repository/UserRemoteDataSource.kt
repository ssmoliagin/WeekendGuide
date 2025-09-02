package com.example.weekendguide.data.repository

import com.example.weekendguide.BuildConfig
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging,
    private val userPreferences: UserPreferences,
) {

    private val usersCollection = firestore.collection("users")
    private val reviewsCollection = firestore.collection("reviews")

    // Sync user data when logging in
    suspend fun syncOnLogin(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val userId = user.uid

        return try {

            val token = messaging.token.await()
            val doc = usersCollection.document(userId).get().await()

            if (doc.exists()) {
                val remoteUserData = doc.toObject(UserData::class.java)
                if (remoteUserData != null) {
                    val updatedUserData = remoteUserData.copy(
                        fcm_token = token
                    )
                    usersCollection.document(userId).set(updatedUserData).await()
                    userPreferences.saveUserData(updatedUserData)
                }
            } else {
                val newUserData = UserData(
                    email = user.email,
                    displayName = if (user.displayName.isNullOrBlank()) {
                        user.email.toString().substringBefore("@")
                    } else {
                        user.displayName
                    },
                    photoUrl = user.photoUrl?.toString(),
                    language = userPreferences.getLanguage(),
                    fcm_token = token,
                )
                usersCollection.document(userId).set(newUserData).await()
                userPreferences.saveUserData(newUserData)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Launch local-to-remote sync in coroutine scope
    fun launchSyncLocalToRemote(scope: CoroutineScope) {
        scope.launch {
            try {
                syncLocalToRemote()
            } catch (_: Exception) {
            }
        }
    }

    // Upload local preferences to Firestore
    suspend fun syncLocalToRemote() {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        val localData = userPreferences.userDataFlow.first()
        usersCollection.document(user.uid).set(localData).await()
    }

    suspend fun deleteUserFromFirestore(userId: String) {
        usersCollection.document(userId).delete().await()
    }

    // Submit new review
    suspend fun submitReview(review: Review) {
        try {
            reviewsCollection.add(review).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Get all reviews for a specific POI
    suspend fun getReviewsForPoi(poiId: String): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("poiId", poiId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Review::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get all reviews in the system
    suspend fun getAllReviews(): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Review::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
