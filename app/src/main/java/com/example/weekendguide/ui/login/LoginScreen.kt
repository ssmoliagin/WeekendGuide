package com.example.weekendguide.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.BuildConfig
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.SplashViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: (SplashViewModel.Destination) -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = remember { FirebaseAuth.getInstance() }
    val oneTapClient = remember { Identity.getSignInClient(context) }
    val preferences = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun checkRegionAndNavigate() {
        val region = preferences.getSelectedRegion()
        if (region != null) {
            onNavigate(SplashViewModel.Destination.Main)
        } else {
            onNavigate(SplashViewModel.Destination.RegionSelect)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            isLoading = true
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity) { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        coroutineScope.launch {
                            checkRegionAndNavigate()
                        }
                    } else {
                        errorMessage = "Google sign-in failed"
                    }
                }
        } catch (e: Exception) {
            Log.e("LoginScreen", "Google sign-in failed", e)
            isLoading = false
            errorMessage = "Google sign-in error"
        }
    }

    fun startGoogleSignIn() {
        isLoading = true
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                launcher.launch(intentSenderRequest)
            }
            .addOnFailureListener {
                Log.e("LoginScreen", "Google sign-in initiation failed", it)
                isLoading = false
                errorMessage = "Не удалось начать вход через Google"
            }
    }

    LaunchedEffect(Unit) {
        auth.currentUser?.let {
            checkRegionAndNavigate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekend Guide") },
                actions = {
                    IconButton(onClick = { /* открыть профиль */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Профиль")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Вход в Weekend Guide", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        isLoading = true
                        errorMessage = null

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                isLoading = false
                                coroutineScope.launch {
                                    checkRegionAndNavigate()
                                }
                            }
                            .addOnFailureListener {
                                // если не найден — регистрируем
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        coroutineScope.launch {
                                            checkRegionAndNavigate()
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        isLoading = false
                                        errorMessage = error.localizedMessage ?: "Ошибка входа/регистрации"
                                    }
                            }
                    }) {
                        Text("Войти или зарегистрироваться")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        startGoogleSignIn()
                    }) {
                        Text("Войти через Google")
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
