package com.example.weekendguide.ui.components

import android.view.SoundEffectConstants
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onShowFavoritesList: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenStatistics: () -> Unit,
    onDismiss: () -> Unit
) {
    val sound = LocalView.current

    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == "main",
            onClick = {
                onDismiss()
                onItemSelected("main")
                sound.playSoundEffect(SoundEffectConstants.CLICK)
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            label = { Text("Поиск") }
        )

        NavigationBarItem(
            selected = selectedItem == "favorites",
            onClick = {
                onDismiss()
                onItemSelected("favorites")
                sound.playSoundEffect(SoundEffectConstants.CLICK)
                onShowFavoritesList()
            },
            icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Избранное") },
            label = { Text("Избранное") }
        )

        NavigationBarItem(
            selected = selectedItem == "statistics",
            onClick = {
                onDismiss()
                onItemSelected("statistics")
                sound.playSoundEffect(SoundEffectConstants.CLICK)
                onOpenStatistics()
            },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Достижения") },
            label = { Text("Достижения") }
        )

        NavigationBarItem(
            selected = selectedItem == "profile",
            onClick = {
                onDismiss()
                onItemSelected("profile")
                sound.playSoundEffect(SoundEffectConstants.CLICK)
                onOpenProfile()
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label = { Text("Профиль") }
        )
    }
}