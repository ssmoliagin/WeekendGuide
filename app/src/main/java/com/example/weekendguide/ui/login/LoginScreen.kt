package com.example.weekendguide.ui.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.LoginViewModelFactory
import com.example.weekendguide.viewmodel.SplashViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onNavigate: (SplashViewModel.Destination) -> Unit,
    ) {

    val context = LocalContext.current

    val activity = context as Activity

    val isLoading by loginViewModel.isLoading.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val userInfo by loginViewModel.userInfo.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val languages = listOf("ru", "en", "de")

    var selectedLanguage by remember { mutableStateOf("ru") }
    var expanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Навигация при изменении destination
    val navigateDestination = remember { mutableStateOf<SplashViewModel.Destination?>(null) }
    LaunchedEffect(Unit) {
        loginViewModel.navigateDestination.collect { destination ->
            onNavigate(destination)
        }
    }

    // При старте проверяем залогинен ли пользователь
    LaunchedEffect(Unit) {
        loginViewModel.checkAlreadyLoggedIn()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loginViewModel.handleGoogleSignInResult(result.data)
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
                        loginViewModel.loginWithEmail(email, password)
                    }) {
                        Text("Войти или зарегистрироваться")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        loginViewModel.startGoogleSignIn { intentSenderRequest ->
                            launcher.launch(intentSenderRequest)
                        }
                    }) {
                        Text("Войти через Google")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Выберите язык интерфейса")
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(selectedLanguage.uppercase())
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {

                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language.uppercase()) },
                                    onClick = {
                                        selectedLanguage = language
                                        expanded = false
                                    }
                                )
                            }
                        }
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