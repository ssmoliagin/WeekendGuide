package com.example.weekendguide.viewmodel

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val userPreferences: UserPreferences,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val userData: StateFlow<UserData> = userPreferences.userDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserData()
    )

    private val _navigateDestination = MutableSharedFlow<SplashViewModel.Destination>()
    val navigateDestination = _navigateDestination.asSharedFlow()

    fun checkRegionAndNavigate() {
        viewModelScope.launch {
            val regions = userPreferences.getCollectionRegions()
            if (regions.isNotEmpty()) {
                _navigateDestination.emit(SplashViewModel.Destination.Main)
            } else {
                _navigateDestination.emit(SplashViewModel.Destination.Store)
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    val res = userRemoteDataSource.syncOnLogin()
                    if (res.isSuccess) {
                        checkRegionAndNavigate()
                    } else {
                        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Ошибка синхронизации"
                    }
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            val res = userRemoteDataSource.syncOnLogin()
                            if (res.isSuccess) {
                                checkRegionAndNavigate()
                            } else {
                                _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Ошибка синхронизации"
                            }
                            _isLoading.value = false
                        }
                    }
                    .addOnFailureListener { error ->
                        _isLoading.value = false
                        _errorMessage.value = error.localizedMessage ?: "Ошибка входа/регистрации"
                    }
            }
    }

    fun startGoogleSignIn(
        onIntentSenderReady: (IntentSenderRequest) -> Unit
    ) {
        _isLoading.value = true
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(Constants.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                onIntentSenderReady(IntentSenderRequest.Builder(result.pendingIntent).build())
            }
            .addOnFailureListener {
                _isLoading.value = false
                _errorMessage.value = "Не удалось начать вход через Google"
            }
    }

    fun handleGoogleSignInResult(intent: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val idToken = credential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            _isLoading.value = true
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            val res = userRemoteDataSource.syncOnLogin()
                            if (res.isSuccess) {
                                checkRegionAndNavigate()
                            } else {
                                _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Ошибка синхронизации"
                            }
                        }
                    } else {
                        _errorMessage.value = "Google sign-in failed"
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Google sign-in error"
        }
    }

    private suspend fun saveUserToPreferences(user: FirebaseUser?) {
        if (user == null) return

        // 🔁 Обновляем также в Firestore
        val data = UserData(
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl?.toString()
        )
        userPreferences.saveUserData(data)
        userRemoteDataSource.syncLocalToRemote()
    }


    fun checkAlreadyLoggedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                val res = userRemoteDataSource.syncOnLogin()
                if (res.isSuccess) {
                    checkRegionAndNavigate()
                } else {
                    _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Ошибка синхронизации"
                }
            }
        }
    }
}