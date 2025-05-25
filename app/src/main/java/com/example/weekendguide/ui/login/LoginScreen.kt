package com.example.weekendguide.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit
) {
    var isLoggingIn by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoggingIn) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Вход через Google", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    isLoggingIn = true
                    // Здесь вызов логина через Firebase, затем:
                    // при успешном входе:
                    onNavigate("region") // например, перейти к выбору региона
                }) {
                    Text("Войти")
                }
            }
        }
    }
}
