package com.example.weekendguide.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersButtons(
    userCurrentCity: String?,
    onRequestGPS: () -> Unit,
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    onOpenMapScreen: () -> Unit,
    onOpenListScreen: () -> Unit,
    onOpenFilters: () -> Unit,
    onShowScreenType: String? = null,
    onDismiss: () -> Unit,
    radiusValues: List<String>,
    currentUnits: String
) {
    val sound = LocalView.current

    val buttonHeight = 30.dp
    var radiusExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        val commonButtonColors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White // Белый фон для всех кнопок
        )

        if (onShowScreenType == "map") {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    onOpenListScreen()
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Список", fontSize = 13.sp, maxLines = 1)
            }
        } else {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    if (userCurrentCity == null) {
                        onRequestGPS()
                    } else {
                        onOpenMapScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("На карте", fontSize = 13.sp, maxLines = 1)
            }
        }

        // 🎯 Кнопка с выпадающим списком радиуса
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { radiusExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(" + $selectedRadius $currentUnits", fontSize = 13.sp, maxLines = 1)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // ⬇ Выпадающее меню
            DropdownMenu(
                expanded = radiusExpanded,
                onDismissRequest = { radiusExpanded = false },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.TopCenter)
            ) {
                radiusValues.forEach { radius ->
                    DropdownMenuItem(
                        text = { Text("$radius $currentUnits") },
                        onClick = {
                            sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                            onRadiusChange(radius)
                            radiusExpanded = false
                        }
                    )
                }
            }
        }

        // ⚙️ Кнопка "Фильтр"
        OutlinedButton(
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onOpenFilters()
            },
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = commonButtonColors
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Фильтр", fontSize = 13.sp, maxLines = 1)
        }
    }
}