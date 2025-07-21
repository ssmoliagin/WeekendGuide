package com.example.weekendguide.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.ui.poi.POICard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    userCurrentCity: String?,
    onOpenListScreen: () -> Unit,
    onSelectSingleType: (String) -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    onFavoriteClick: (String) -> Unit,
    onPOIClick: () -> Unit,
    onSelectPOI: (POI) -> Unit,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    showLocationPanel: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,
    showStoreBanner: @Composable () -> Unit,
    allReviews: List<Review> = emptyList(),
    currentUnits: String,
    currentLanguage: String,
) {
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Scaffold(
        topBar = { showTopAppBar() },
        bottomBar = { showNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            showLocationPanel()
            Spacer(modifier = Modifier.height(4.dp))
            showFiltersButtons()
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    val displayedPOIs = userPOIList.shuffled().take(10)
                    val count = userPOIList.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${count} ${LocalizerUI.t("places_near", currentLanguage)} ${userCurrentCity ?: LocalizerUI.t("you", currentLanguage)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = LocalizerUI.t("show_all", currentLanguage),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onOpenListScreen() }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(displayedPOIs) { poi ->
                            Box(modifier = Modifier.width(screenWidth - 32.dp)) {
                                POICard(
                                    poi = poi,
                                    isFavorite = isFavorite(poi),
                                    isVisited = isVisited(poi),
                                    onFavoriteClick = { onFavoriteClick(poi.id) },
                                    userLocation = userLocation,
                                    userCurrentCity = userCurrentCity,
                                    cardType = "list",
                                    onClick = onPOIClick,
                                    onSelectPOI = onSelectPOI,
                                    reviews = allReviews.filter { it.poiId == poi.id },
                                    currentUnits = currentUnits,
                                    currentLanguage = currentLanguage,
                                )
                            }
                        }
                    }
                }

                val types = userPOIList.map { it.type }.toSet().filter { it.isNotBlank() }
                types.forEach { type ->
                    item {
                        val allTypedPOIs = userPOIList.filter { it.type == type }
                        val displayedPOIs = allTypedPOIs.shuffled().take(6)
                        val count = allTypedPOIs.size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${LocalizerUI.t(type, currentLanguage).replaceFirstChar { it.uppercaseChar() }} ${LocalizerUI.t("near", currentLanguage)} ${userCurrentCity ?: LocalizerUI.t("you", currentLanguage)} - $count",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = LocalizerUI.t("show_all", currentLanguage),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onSelectSingleType(type)
                                    onOpenListScreen()
                                }
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(displayedPOIs) { poi ->
                                POICard(
                                    poi = poi,
                                    isFavorite = isFavorite(poi),
                                    isVisited = isVisited(poi),
                                    onFavoriteClick = { onFavoriteClick(poi.id) },
                                    userLocation = userLocation,
                                    userCurrentCity = userCurrentCity,
                                    cardType = "mini",
                                    onClick = onPOIClick,
                                    onSelectPOI = onSelectPOI,
                                    reviews = allReviews.filter { it.poiId == poi.id },
                                    currentUnits = currentUnits,
                                    currentLanguage = currentLanguage,
                                )
                            }
                        }
                    }
                }

                item {
                    showStoreBanner()
                }
            }
        }
    }
}