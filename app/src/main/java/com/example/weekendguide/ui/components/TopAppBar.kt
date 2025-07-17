package com.example.weekendguide.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar (
    currentGP: Int,
    onItemSelected: (String) -> Unit,
    topBarTitle: String,
    onDismiss: () -> Unit
) {
    val sound = LocalView.current

    val title = when (topBarTitle) {
        "main" -> "Weekend Guide"
        "favorites" -> "Избранное"
        "statistic" -> "Достижения"
        "profile" -> "Профиль"
        else -> topBarTitle
    }

    TopAppBar(
        title = { Text(title, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$currentGP 🏆", color = Color.White)
            }
        },
        navigationIcon = {
            if (topBarTitle != "main") {
                IconButton(onClick = {
                    sound.playSoundEffect(SoundEffectConstants.CLICK)
                    onItemSelected("main")
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }
        },
    )
}