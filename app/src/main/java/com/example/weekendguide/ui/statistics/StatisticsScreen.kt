package com.example.weekendguide.ui.statistics

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.locales.LocalizerTypes
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    totalGP: Int,
    currentGP: Int,
    spentGP: Int,
    userPOIList: List<POI>,
    totalPOIList: List<POI>,
    allTypes: List<String>,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    pointsViewModel: PointsViewModel,
    translateViewModel: TranslateViewModel,
) {
    val currentLanguage by translateViewModel.language.collectAsState()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    val typeStats = userPOIList.groupingBy { it.type }.eachCount()
    val leveledUpSet = remember { mutableStateMapOf<String, Int>() }

    var purchasedRegionsCount by remember { mutableStateOf(0) }
    var purchasedCountriesCount by remember { mutableStateOf(0) }

    // Загрузка достигнутых уровней
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val savedLevels = prefs.getCategoryLevels()
            savedLevels.forEach { (category, level) ->
                leveledUpSet[category] = level
            }
        }
        // Загрузка количество регионов и стран (временое решение пока не переделаю poi.csv)
        purchasedRegionsCount = prefs.getPurchasedRegions().size
        purchasedCountriesCount = prefs.getPurchasedCountries().size
    }

    val totalPOIs = totalPOIList.size
    val visitedPOIs = userPOIList.size

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

    val typeGoals = listOf(5, 10, 20, 50, 100) // мест до следущего уровня

    Scaffold(
        topBar = { showTopAppBar() },
        bottomBar = { showNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {
                // Блок 1: Очки
                Text(
                    text = "🏆 Всего очков",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("🟢 Набрано:", Modifier.weight(1f))
                            Text("$totalGP GP", fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("🔴 Потрачено:", Modifier.weight(1f))
                            Text("$spentGP GP", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Блок 2: Статистика
                Text(
                    text = "🧭 Общая статистика",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("\uD83C\uDF0D Стран посещено:", Modifier.weight(1f))
                            Text(
                                "$purchasedCountriesCount",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("🚩 Регионов открыто:", Modifier.weight(1f))
                            Text(
                                "$purchasedRegionsCount",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("✅ Посещено мест:", Modifier.weight(1f))
                            Text(
                                "$visitedPOIs / $totalPOIs",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "🎯 Достижения по категориям",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(allTypes) { type ->
                val count = typeStats[type] ?: 0
                val level = typeGoals.indexOfFirst { count < it }.let { if (it == -1) typeGoals.size else it }
                val currentGoal = typeGoals.getOrNull(level) ?: typeGoals.last()
                val nextGoal = currentGoal - count
                val percent = (count * 100 / currentGoal).coerceAtMost(100)
                val points = 1000// * (level + 1)
                val icon = typeIcons[type] ?: Icons.Default.Star

                val savedLevel = leveledUpSet[type] ?: 0
                val isNewLevelReached = level > savedLevel
                var showCongrats by remember { mutableStateOf(false) }

                if (showCongrats) {
                    LaunchedEffect(Unit) {
                        delay(2500)
                        showCongrats = false
                    }
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = percent / 100f,
                    animationSpec = tween(durationMillis = 700),
                    label = "Animated Progress"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNewLevelReached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp) // скругление
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = type,
                                modifier = Modifier
                                    .size(40.dp)  // увеличил размер иконки
                                    .padding(end = 16.dp),
                                tint = if (isNewLevelReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${LocalizerTypes.t(type, currentLanguage).replaceFirstChar { it.uppercaseChar() }} - $count",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isNewLevelReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Уровень ${level + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = animatedProgress,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Text(
                            text = "$nextGoal до следующего уровня",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isNewLevelReached) {
                            Button(
                                onClick = {
                                    showCongrats = true
                                    coroutineScope.launch {
                                        prefs.levelUpCategory(type, level, points)
                                        leveledUpSet[type] = level
                                        pointsViewModel.addGP(1000)
                                        Toast.makeText(context, "+$points GP за новый уровень!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Level Up!", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        if (showCongrats) {
                            Text(
                                text = "🎉 +$points GP!",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}