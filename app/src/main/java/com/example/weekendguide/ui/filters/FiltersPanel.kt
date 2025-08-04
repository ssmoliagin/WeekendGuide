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
import androidx.compose.material.icons.automirrored.filled.Label
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
    currentLanguage: String,
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    radiusValues: List<String>,
    currentUnits: String,
    allTypes: List<String>,
    selectedTypes: List<String>,
    onTypeToggle: (String) -> Unit,
    onSelectAllTypes: () -> Unit,
    onClearAllTypes: () -> Unit,
    typeIcons: Map<String, ImageVector>,
    allTags: List<String>,
    selectedTags: List<String>,
    onTagToggle: (String) -> Unit,
    onSelectAllTags: () -> Unit,
    onClearAllTags: () -> Unit,
    tagsIcons: Map<String, ImageVector>,
    showVisited: Boolean,
    onToggleShowVisited: () -> Unit,
    sortType: String,
    onSortTypeChange: (String) -> Unit,
) {


    val radiusSliderPosition = radiusValues.indexOf(selectedRadius).coerceAtLeast(0)
    val allTypesSelected = selectedTypes.containsAll(allTypes)
    val allTagsSelected = selectedTags.containsAll(allTags)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 🔵 Radius selector
            Text("${LocalizerUI.t("radius", currentLanguage)} ($currentUnits)",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium)

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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )

            // ✅ Place types
            Text(LocalizerUI.t("place_types", currentLanguage),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium)

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
                                        color = MaterialTheme.colorScheme.onBackground,
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
                        if (allTypesSelected) onClearAllTypes() else onSelectAllTypes()
                    },
                    label = {
                        Text(
                            LocalizerUI.t(
                                if (allTypesSelected) "clear_all" else "select_all",
                                currentLanguage
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (allTypesSelected) Icons.Default.Clear else Icons.Default.DoneAll,
                            contentDescription = null
                        )
                    }
                )
            }

            // 🏷️ Place Tags
            Text(LocalizerUI.t("place_tags", currentLanguage),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium)

            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTags.forEach { tag ->
                    val icon = tagsIcons[tag] ?: Icons.AutoMirrored.Filled.Label
                    FilterChip(
                        selected = selectedTags.contains(tag),
                        onClick = { onTagToggle(tag) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tag,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = LocalizerUI.t(tag, currentLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }

                AssistChip(
                    onClick = {
                        if (allTagsSelected) onClearAllTags() else onSelectAllTags()
                    },
                    label = {
                        Text(
                            LocalizerUI.t(
                                if (allTagsSelected) "clear_all" else "select_all",
                                currentLanguage
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (allTagsSelected) Icons.Default.Clear else Icons.Default.DoneAll,
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
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Sort
            Text(
                LocalizerUI.t("sort_by", currentLanguage),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "distance" to LocalizerUI.t("distance", currentLanguage),
                    "name" to LocalizerUI.t("name", currentLanguage),
                    "rating" to LocalizerUI.t("rating", currentLanguage)
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = sortType == key,
                        onClick = { onSortTypeChange(key) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}
