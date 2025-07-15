package com.example.weekendguide.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.ui.poi.POICard
import com.example.weekendguide.viewmodel.POIViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPOIScreen(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    userCurrentCity: String?,
    onSelectPOI: (POI) -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    onFavoriteClick: (String) -> Unit,
    onPOIClick: () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    showNavigationBar: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,
    allReviews: List<Review> = emptyList(),
    poiViewModel: POIViewModel
) {
    val listState = rememberLazyListState()

    Scaffold(

        //ШАПКА
        topBar = {
            showTopAppBar()
        },
        //НИЖНЕЕ МЕНЮ
        bottomBar = {
            showNavigationBar()
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // кнопки фильтры

            showFiltersButtons()

            Spacer(modifier = Modifier.height(12.dp))

            // 📋 Список POI

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(userPOIList) { poi ->
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
                        poiViewModel = poiViewModel
                    )
                }
            }
        }
    }
}