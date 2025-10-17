package com.weekendguide.app.ui.login

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.weekendguide.app.Constants.APP_DOCS_URL
import com.weekendguide.app.R
import com.weekendguide.app.data.locales.LocalizerUI
import com.weekendguide.app.data.model.UserData
import com.weekendguide.app.viewmodel.LoginViewModel
import com.weekendguide.app.viewmodel.SplashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userData: UserData,
    loginViewModel: LoginViewModel,
    onNavigate: (SplashViewModel.Destination) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val currentLanguage = userData.language?:"en"

    val isLoading by loginViewModel.isLoading.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loginViewModel.handleGoogleSignInResult(result.data)
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.navigateDestination.collect { destination ->
            onNavigate(destination)
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.checkAlreadyLoggedIn()
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = LocalizerUI.t("login_title", currentLanguage),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                loginViewModel.startGoogleSignIn { intentSenderRequest ->
                                    launcher.launch(intentSenderRequest)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(start = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.CenterVertically),
                                    tint = Color.Unspecified
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(LocalizerUI.t("continue_google", currentLanguage))
                                }

                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = LocalizerUI.t("or", currentLanguage),
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(LocalizerUI.t("email", currentLanguage)) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(LocalizerUI.t("password", currentLanguage)) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.fillMaxWidth(0.9f),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (email.isNotBlank() && password.isNotBlank()) {
                                            loginViewModel.loginWithEmail(email, password)
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    loginViewModel.loginWithEmail(email, password)
                                },
                                enabled = email.isNotBlank() && password.isNotBlank()
                            ) {
                                Text(LocalizerUI.t("continue_email", currentLanguage), fontSize = 14.sp)
                            }

                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = LocalizerUI.t("terms_acceptance", currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, APP_DOCS_URL.toUri())
                                context.startActivity(intent)
                            }
                    )
                }
            }
        }
    }
}

