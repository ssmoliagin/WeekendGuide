package com.weekendguide.app.viewmodel

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weekendguide.app.Constants
import com.weekendguide.app.data.preferences.UserPreferences
import com.weekendguide.app.data.repository.UserRemoteDataSource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.weekendguide.app.service.BillingManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModelFactory(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val billingManager: BillingManager,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(auth, oneTapClient, userPreferences, userRemote, subscriptionViewModel, billingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val userPreferences: UserPreferences,
    private val userRemote: UserRemoteDataSource,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _navigateDestination = MutableSharedFlow<SplashViewModel.Destination>()
    val navigateDestination = _navigateDestination.asSharedFlow()

    fun appNavigation() {
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
                postLoginSync()
            }
            .addOnFailureListener {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        postLoginSync()
                    }
                    .addOnFailureListener { error ->
                        _isLoading.value = false
                        _errorMessage.value = error.localizedMessage ?: "Login/registration failed"
                    }
            }
    }

    fun startGoogleSignIn(onIntentSenderReady: (IntentSenderRequest) -> Unit) {
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
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Google sign-in failed: ${e.localizedMessage}"
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
                        postLoginSync()
                    } else {
                        _errorMessage.value = "Google sign-in failed"
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Google sign-in error"
        }
    }

    fun checkAlreadyLoggedIn() {
        if (auth.currentUser != null) {
            postLoginSync()
        }
    }

    private fun postLoginSync() {
        viewModelScope.launch {
            val res = userRemote.syncOnLogin()
            if (res.isSuccess) {

                val savedToken = userPreferences.userDataFlow.first().subToken
                if (!savedToken.isNullOrEmpty()) {
                    billingManager.validateSavedSubscriptionToken(savedToken) { isActive ->
                        subscriptionViewModel.setSubscriptionEnabled(isActive, if (isActive) savedToken else null)
                    }
                }

                appNavigation()
            } else {
                _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Sync error"
            }
            _isLoading.value = false
        }
    }
}
