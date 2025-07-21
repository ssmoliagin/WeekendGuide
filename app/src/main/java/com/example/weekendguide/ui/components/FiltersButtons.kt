package com.example.weekendguide.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weekendguide.data.locales.LocalizerUI

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
    currentUnits: String,
    currentLanguage: String,
) {
    val sound = LocalView.current
    val buttonHeight = 30.dp
    var radiusExpanded by remember { mutableStateOf(false) }

    val commonButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.White
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (onShowScreenType == "map") {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(SoundEffectConstants.CLICK)
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
                Text(LocalizerUI.t("list", currentLanguage), fontSize = 13.sp, maxLines = 1)
            }
        } else {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(SoundEffectConstants.CLICK)
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
                Text(LocalizerUI.t("on_map", currentLanguage), fontSize = 13.sp, maxLines = 1)
            }
        }

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
                            sound.playSoundEffect(SoundEffectConstants.CLICK)
                            onRadiusChange(radius)
                            radiusExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                sound.playSoundEffect(SoundEffectConstants.CLICK)
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
            Text(LocalizerUI.t("filter", currentLanguage), fontSize = 13.sp, maxLines = 1)
        }
    }
}
