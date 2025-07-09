package com.example.weekendguide.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.locales.LocalizerTypes
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.StatisticsViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    userPOIList: List<POI>,
    totalPOIList: List<POI>,
    allTypes: List<String>,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    pointsViewModel: PointsViewModel,
    translateViewModel: TranslateViewModel,
    statisticsViewModel: StatisticsViewModel
) {
    val context = LocalContext.current
    val currentLanguage by translateViewModel.language.collectAsState()

    val totalGP by pointsViewModel.totalGP.collectAsState()
    val spentGP by pointsViewModel.spentGP.collectAsState()
    val purchasedRegionsCount by statisticsViewModel.purchasedRegionsCount.collectAsState()
    val purchasedCountriesCount by statisticsViewModel.purchasedCountriesCount.collectAsState()
    val typeStats = userPOIList.groupingBy { it.type }.eachCount()
    val leveledUpSet by statisticsViewModel.categoryLevels.collectAsState()
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

    val typeGoals = listOf(5, 10, 20, 50, 100)

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
                            Text("$purchasedCountriesCount", fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("🚩 Регионов открыто:", Modifier.weight(1f))
                            Text("$purchasedRegionsCount", fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("✅ Посещено мест:", Modifier.weight(1f))
                            Text("$visitedPOIs / $totalPOIs", fontWeight = FontWeight.Bold)
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
                val percent = (count * 100 / currentGoal).coerceAtMost(100)
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
                    shape = RoundedCornerShape(16.dp)
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
                                    .size(40.dp)
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
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}
