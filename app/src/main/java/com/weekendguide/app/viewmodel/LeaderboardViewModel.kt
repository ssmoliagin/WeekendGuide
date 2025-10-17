package com.weekendguide.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModelFactory(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            return LeaderboardViewModel(auth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LeaderboardViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _leaderboardVisible = MutableStateFlow(false)
    val leaderboardVisible: StateFlow<Boolean> = _leaderboardVisible.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val leaderboard: StateFlow<List<Pair<String, Int>>> = _leaderboard.asStateFlow()

    private val _userRank = MutableStateFlow<Int?>(null)
    val userRank: StateFlow<Int?> = _userRank.asStateFlow()

    fun toggleLeaderboardVisibility() {
        _leaderboardVisible.value = !_leaderboardVisible.value
    }

    fun loadLeaderboard() {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()

                val users = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("displayName") ?: "Anonymous"
                    val gp = doc.getLong("total_GP")?.toInt() ?: 0
                    val id = doc.id
                    Triple(id, name, gp)
                }.sortedByDescending { it.third }

                _leaderboard.value = users.map { it.second to it.third }

                val rank = users.indexOfFirst { it.first == currentUserId }
                _userRank.value = if (rank != -1) rank + 1 else null

            } catch (_: Exception) {

            }
        }
    }
}