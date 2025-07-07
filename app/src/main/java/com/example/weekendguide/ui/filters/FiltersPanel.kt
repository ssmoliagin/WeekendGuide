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
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weekendguide.data.locales.LocalizerTypes
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.viewmodel.TranslateViewModel

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
    translateViewModel: TranslateViewModel,
    radiusValues: List<String>,
    currentUnits: String
) {
    val currentLanguage by translateViewModel.language.collectAsState()
    val radiusSliderPosition = radiusValues.indexOf(selectedRadius).coerceAtLeast(0)

    val typeIcons = mapOf(
        "castle" to Icons.Default.Castle,
        "nature" to Icons.Default.Forest,
        "park" to Icons.Default.NaturePeople,
        "funpark" to Icons.Default.Attractions,
        "museum" to Icons.Default.Museum,
        "swimming" to Icons.Default.Pool,
        "hiking" to Icons.Default.DirectionsWalk,
        "cycling" to Icons.Default.DirectionsBike,
        "zoo" to Icons.Default.Pets,
        "city-walk" to Icons.Default.LocationCity,
        "festival" to Icons.Default.Celebration,
        "active" to Icons.Default.DownhillSkiing
    )

    val allSelected = selectedTypes.containsAll(allTypes)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 🔵 Радиус
            Text("Дальность (км)", style = MaterialTheme.typography.titleMedium)

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
                text = "Выбрано: $selectedRadius $currentUnits",
                modifier = Modifier.padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )

            // ✅ Типы мест
            Text("Типы мест", style = MaterialTheme.typography.titleMedium)

            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTypes.forEach { type ->
                    val icon = typeIcons[type]
                    FilterChip(
                        selected = selectedTypes.contains(type),
                        onClick = { onTypeToggle(type) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = type,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = LocalizerTypes.t(type, currentLanguage),//type,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                } else {
                                    //Text(type)
                                    Text(LocalizerTypes.t(type, currentLanguage))
                                }
                            }
                        }
                    )
                }

                // 🔁 Кнопка "Выбрать все / Убрать все"
                AssistChip(
                    onClick = {
                        if (allSelected) onClearAllTypes() else onSelectAllTypes()
                    },
                    label = {
                        Text(if (allSelected) "Убрать все" else "Выбрать все")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (allSelected) Icons.Default.Clear else Icons.Default.DoneAll,
                            contentDescription = null
                        )
                    }
                )
            }

            // ☑️ Показывать посещённые
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
                Text("Показывать посещённые", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}