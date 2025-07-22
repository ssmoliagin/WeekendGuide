package com.example.weekendguide.ui.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weekendguide.data.locales.LocalizerUI

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersPanel(
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    allTypes: List<String>,
    selectedTypes: List<String>,
    onTypeToggle: (String) -> Unit,
    onSelectAllTypes: () -> Unit,
    onClearAllTypes: () -> Unit,
    showVisited: Boolean,
    onToggleShowVisited: () -> Unit,
    radiusValues: List<String>,
    currentLanguage: String,
    currentUnits: String,
    typeIcons: Map<String, ImageVector>,
) {

    val radiusSliderPosition = radiusValues.indexOf(selectedRadius).coerceAtLeast(0)
    val allSelected = selectedTypes.containsAll(allTypes)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 🔵 Radius selector
            Text("${LocalizerUI.t("radius", currentLanguage)} ($currentUnits)", style = MaterialTheme.typography.titleMedium)

            Slider(
                value = radiusSliderPosition.toFloat(),
                onValueChange = {
                    val selected = radiusValues[it.toInt()]
                    onRadiusChange(selected)
                },
                steps = radiusValues.size - 2,
                valueRange = 0f..(radiusValues.size - 1).toFloat()
            )

            Text(
                text = "${LocalizerUI.t("selected_radius", currentLanguage)}: $selectedRadius ${LocalizerUI.t(currentUnits, currentLanguage)}",
                modifier = Modifier.padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )

            // ✅ Place types
            Text(LocalizerUI.t("place_types", currentLanguage), style = MaterialTheme.typography.titleMedium)

            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTypes.forEach { type ->
                    val icon = typeIcons[type] ?: Icons.Default.Star
                    FilterChip(
                        selected = selectedTypes.contains(type),
                        onClick = { onTypeToggle(type) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = type,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = LocalizerUI.t(type, currentLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                            }
                        }
                    )
                }

                // 🔁 Select all / Clear all
                AssistChip(
                    onClick = {
                        if (allSelected) onClearAllTypes() else onSelectAllTypes()
                    },
                    label = {
                        Text(
                            LocalizerUI.t(
                                if (allSelected) "clear_all" else "select_all",
                                currentLanguage
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (allSelected) Icons.Default.Clear else Icons.Default.DoneAll,
                            contentDescription = null
                        )
                    }
                )
            }

            // ☑️ Show visited checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onToggleShowVisited() }
            ) {
                Checkbox(
                    checked = showVisited,
                    onCheckedChange = { onToggleShowVisited() }
                )
                Text(
                    LocalizerUI.t("show_visited", currentLanguage),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
