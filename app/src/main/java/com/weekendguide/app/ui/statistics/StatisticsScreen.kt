package com.weekendguide.app.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weekendguide.app.data.locales.LocalizerUI
import com.weekendguide.app.data.model.POI
import com.weekendguide.app.data.model.UserData
import com.weekendguide.app.viewmodel.LeaderboardViewModel
import com.weekendguide.app.viewmodel.StatisticsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    userPOIList: List<POI>,
    totalPOIList: List<POI>,
    allTypes: List<String>,
    totalGP: Int,
    currentGP: Int,
    onOpenListScreen: () -> Unit,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    statisticsViewModel: StatisticsViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    typeIcons: Map<String, ImageVector>,
    leveledUpSet: Map<String, Int>,
    purchasedRegionsCount: Int,
    purchasedCountriesCount: Int,
    userData: UserData,

) {
    // --- UserData State ---
    val currentLanguage = userData.language?:"en"

    val typeStats = userPOIList.groupingBy { it.type }.eachCount()
    val totalPOIs = totalPOIList.size
    val visitedPOIs = userPOIList.size
    val typeGoals = listOf(5, 10, 20, 30, 50, 100, 250, 500, 750, 1000)
    val leaderboardVisible by leaderboardViewModel.leaderboardVisible.collectAsState()
    val userRank by leaderboardViewModel.userRank.collectAsState()
    val leaderboard by leaderboardViewModel.leaderboard.collectAsState()

    LaunchedEffect(Unit) {
        leaderboardViewModel.loadLeaderboard()
    }

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
                    text = LocalizerUI.t("total_points", currentLanguage),
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
                            Text(LocalizerUI.t("currentPoints", currentLanguage),
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text("$currentGP", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(LocalizerUI.t("totalPoints", currentLanguage),
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text("$totalGP", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(LocalizerUI.t("your_rank", currentLanguage),
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text(userRank?.toString() ?: "—", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                        Text(
                            text = if (leaderboardVisible) LocalizerUI.t("hide_leaderboard", currentLanguage) else LocalizerUI.t("show_leaderboard", currentLanguage),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    leaderboardViewModel.toggleLeaderboardVisibility()
                                    if (!leaderboardVisible) leaderboardViewModel.loadLeaderboard()
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }

                if (leaderboardVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(LocalizerUI.t("top_users", currentLanguage),
                                style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(8.dp))
                            leaderboard.take(5).forEachIndexed { index, (name, gp) ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}.",
                                        modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onBackground)
                                    Text(name,
                                        modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                                    Text("$gp GP",
                                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = LocalizerUI.t("general_statistics", currentLanguage),
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
                            Text(LocalizerUI.t("countries_visited", currentLanguage),
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text("$purchasedCountriesCount",
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "\uD83D\uDEA9 ${LocalizerUI.t("regions_unlocked", currentLanguage)}",
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text("$purchasedRegionsCount",
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(LocalizerUI.t("places_visited", currentLanguage),
                                Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                            Text("$visitedPOIs / $totalPOIs",
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }

                        Text(
                            text = LocalizerUI.t("show_all", currentLanguage),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onOpenListScreen() }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = LocalizerUI.t("category_achievements", currentLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            val _allTypes = allTypes.filter { it != "interesting" }
            items(_allTypes) { type ->
                val count = typeStats[type] ?: 0
                val level = typeGoals.indexOfFirst { count < it }.let { if (it == -1) typeGoals.size else it }
                val currentGoal = typeGoals.getOrNull(level) ?: typeGoals.last()
                val nextGoal = currentGoal - count
                val percent = (count * 100 / currentGoal).coerceAtMost(100)
                val icon = typeIcons[type] ?: Icons.Default.Star

                val savedLevel = leveledUpSet[type] ?: 0
                val isNewLevelReached = level > savedLevel

                LaunchedEffect(type) {
                    if (isNewLevelReached) {
                        statisticsViewModel.updateRewardAvailable(type)
                    }
                }

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
                                tint = if (isNewLevelReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${LocalizerUI.t(type, currentLanguage).replaceFirstChar { it.uppercaseChar() }} - $count",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isNewLevelReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Text(
                            text = "$nextGoal ${LocalizerUI.t("to_next_level", currentLanguage)} ${level + 2}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isNewLevelReached) {
                            Button(
                                onClick = {
                                    showCongrats = true
                                    statisticsViewModel.updateCategoryLevel(type, level)
                                    //pointsViewModel.addGP(1000)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizerUI.t("level_up", currentLanguage), color = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        if (showCongrats) {
                            Text(
                                text = "🎉 +1000 GP!",
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
