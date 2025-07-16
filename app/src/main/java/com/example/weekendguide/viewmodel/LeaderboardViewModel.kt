package com.example.weekendguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val user = auth.currentUser ?: return
        val userId = user.uid

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .get().await()

                val users = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("displayName") ?: "Аноним"
                    val gp = doc.getLong("total_GP")?.toInt() ?: 0
                    val id = doc.id
                    Triple(id, name, gp)
                }.sortedByDescending { it.third }

                _leaderboard.value = users.map { it.second to it.third }

                val rank = users.indexOfFirst { it.first == userId }
                _userRank.value = if (rank != -1) rank + 1 else null

            } catch (e: Exception) {
                Log.e("Leaderboard", "Ошибка загрузки: ${e.message}")
            }
        }
    }
}
