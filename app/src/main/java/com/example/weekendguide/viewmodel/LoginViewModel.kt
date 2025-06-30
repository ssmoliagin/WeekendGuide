package com.example.weekendguide.viewmodel

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weekendguide.Constants
import com.example.weekendguide.data.preferences.UserData
import com.example.weekendguide.data.preferences.UserPreferences
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
    private val userPreferences: UserPreferences
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

    fun saveLanguage(language: String) {
        viewModelScope.launch {
            userPreferences.saveLanguage(language)
        }
    }

    fun checkRegionAndNavigate() {
        viewModelScope.launch {
            val region = userPreferences.getHomeRegion()
            if (region != null) {
                _navigateDestination.emit(SplashViewModel.Destination.Main)
            } else {
                _navigateDestination.emit(SplashViewModel.Destination.RegionSelect)
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    saveUserToPreferences(auth.currentUser)
                    _isLoading.value = false
                    checkRegionAndNavigate()
                }
            }
            .addOnFailureListener {
                // Если не найден — регистрируем
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            saveUserToPreferences(auth.currentUser)
                            _isLoading.value = false
                            checkRegionAndNavigate()
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
                            saveUserToPreferences(auth.currentUser)
                            checkRegionAndNavigate()
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
        val data = UserData(
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl?.toString()
        )
        userPreferences.saveUserInfo(data)
    }

    fun checkAlreadyLoggedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                saveUserToPreferences(currentUser)
                checkRegionAndNavigate()
            }
        }
    }
}