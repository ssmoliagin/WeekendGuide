package com.example.weekendguide.ui.main

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun MainScreen(context: Context = LocalContext.current) {
    var region by remember { mutableStateOf<Region?>(null) }

    LaunchedEffect(Unit) {
        val prefs = UserPreferences(context)
        region = prefs.getHomeRegion()
    }

    region?.let { reg ->
        val viewModel: POIViewModel = viewModel(
            factory = POIViewModelFactory(context, reg)
        )
        MainContent(viewModel, reg)
    } ?: LoadingScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: POIViewModel, region: Region) {
    val poiList by viewModel.poiList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val radiusOptions = listOf("20км", "50км", "100км", "200км", "∞")
    var selectedRadius by remember { mutableStateOf("20км") }
    var randomPOI by remember { mutableStateOf<POI?>(null) }

    LaunchedEffect(poiList) {
        if (poiList.isNotEmpty() && randomPOI == null) {
            randomPOI = poiList.random()
        }
    }

   // val otherRegions = remember { viewModel.getRegionsByCountry(region.country_code).filter { it.code != region.country_code }.take(4) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekend Guide", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("125 GP", color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Профиль",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { /* Навигация к профилю */ }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Поиск */ },
                    icon = { Text("Поиск") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Сохранённое */ },
                    icon = { Text("Сохранённое") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        if (poiList.isNotEmpty()) {
                            randomPOI = poiList.random()
                        }
                    },
                    icon = { Text("Случайное") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Статистика */ },
                    icon = { Text("Статистика") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                radiusOptions.forEach { radius ->
                    FilterChip(
                        selected = selectedRadius == radius,
                        onClick = { selectedRadius = radius },
                        label = { Text(radius) }
                    )
                }
            }

            OutlinedTextField(
                value = "Рядом с вами",
                onValueChange = { /* Выбор города */ },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Местоположение") },
                trailingIcon = {
                    IconButton(onClick = { /* Открыть фильтры */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Фильтры")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Поиск */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Найти")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text("Рекомендованное место", style = MaterialTheme.typography.titleMedium)
                    randomPOI?.let { poi ->
                        POICard(poi = poi, modifier = Modifier.fillMaxWidth()) // почти на всю ширину
                    }
                }

                val types = poiList.map { it.type }.toSet().filter { it.isNotBlank() }
                types.forEach { type ->
                    item {
                        val typedPOIs = poiList.filter { it.type == type }.shuffled().take(6)
                        Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(typedPOIs) { poi -> POICard(poi) }
                        }
                    }
                }
/*
                item {
                    Text("Наборы точек (POI)", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(otherRegions) { regionPreview ->
                            Box(
                                modifier = Modifier
                                    .size(160.dp, 100.dp)
                                    .background(Color.LightGray)
                            ) {
                                Text(regionPreview.name, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
*/
                /*
                item {
                    Text("Новости и обновления", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Text("Обновление #1", modifier = Modifier.align(Alignment.Center))
                    }
                }

                 */


            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
fun POICard(
    poi: POI,
    modifier: Modifier = Modifier
        .width(220.dp)
        .height(180.dp)
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            poi.imageUrl?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentScale = ContentScale.Crop
                )
                Log.d("КАРТИНКА ЕСТЬ? ","${poi.imageUrl}")
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = poi.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = poi.description,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
