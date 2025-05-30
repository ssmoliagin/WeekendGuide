package com.example.weekendguide.ui.main

import android.app.Application
import android.content.Context
import android.location.Location
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.*
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
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import com.example.weekendguide.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.rememberLazyListState


@Composable
fun MainScreen(context: Context = LocalContext.current) {
    val locationViewModel: LocationViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val region by produceState<Region?>(initialValue = null) {
        val prefs = UserPreferences(context)
        value = prefs.getHomeRegion()
    }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                locationViewModel.detectLocationFromGPS()
            } else {
                Toast.makeText(context, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val userLocation by locationViewModel.location.collectAsState()
    val currentCity by locationViewModel.currentCity.collectAsState()

    region?.let { reg ->
        val viewModel: POIViewModel = viewModel(factory = POIViewModelFactory(context, reg))
        MainContent(
            viewModel = viewModel,
            region = reg,
            userLocation = userLocation,
            currentCity = currentCity,
            onRequestLocationChange = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        locationViewModel.detectLocationFromGPS()
                    }
                    else -> {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            },
            onShowProfile = {
                val prefs = UserPreferences(context)
                coroutineScope.launch {
                    val allPrefs = prefs.getAll()
                    Toast.makeText(context, allPrefs.toString(), Toast.LENGTH_LONG).show()
                }
            }
        )
    } ?: LoadingScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: POIViewModel,
    region: Region,
    userLocation: Pair<Double, Double>?,
    currentCity: String?,
    onRequestLocationChange: () -> Unit,
    onShowProfile: () -> Unit
) {
    val poiList by viewModel.poiList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val radiusOptions = listOf("20км", "50км", "100км", "200км", "∞")
    var selectedRadius by remember { mutableStateOf("200км") }
    var randomPOI by remember { mutableStateOf<POI?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val radiusValue = when (selectedRadius) {
        "20км" -> 20
        "50км" -> 50
        "100км" -> 100
        "200км" -> 200
        "∞" -> Int.MAX_VALUE
        else -> 200
    }

    val filteredPOIList = remember(poiList, userLocation, selectedRadius) {
        if (userLocation == null) poiList
        else poiList.filter { poi ->
            val result = FloatArray(1)
            Location.distanceBetween(userLocation.first, userLocation.second, poi.lat, poi.lng, result)
            val distanceInKm = result[0] / 1000
            distanceInKm <= radiusValue
        }
    }

    LaunchedEffect(filteredPOIList) {
        if (filteredPOIList.isNotEmpty() && randomPOI == null) {
            randomPOI = filteredPOIList.random()
        }
    }

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
                                .clickable { onShowProfile() }
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
                    icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Сохранённое */ },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Сохранённое") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        if (filteredPOIList.isNotEmpty()) {
                            randomPOI = filteredPOIList.random()
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Случайное") }

                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Статистика */ },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Статистика") }
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

            // Блок с фильтрами радиуса
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

            // Поле текущего местоположения
            OutlinedTextField(
                value = currentCity ?: "Определение местоположения...",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Местоположение") },
                trailingIcon = {
                    IconButton(onClick = onRequestLocationChange) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Изменить местоположение")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка Поиск
            Button(
                onClick = { /* Поиск */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Найти")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Основной список
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text("Рекомендованное место", style = MaterialTheme.typography.titleMedium)
                    randomPOI?.let { poi ->
                        POICard(poi = poi, userLocation = userLocation, modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp))
                    }
                }

                val types = filteredPOIList.map { it.type }.toSet().filter { it.isNotBlank() }
                types.forEach { type ->
                    item {
                        val typedPOIs = filteredPOIList.filter { it.type == type }.shuffled().take(6)
                        Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(typedPOIs) { poi ->
                                POICard(poi = poi, userLocation = userLocation)
                            }
                        }
                    }
                }
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
    userLocation: Pair<Double, Double>? = null,
    modifier: Modifier = Modifier
        .width(220.dp)
        .height(180.dp)
) {
    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }

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

                distanceKm?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Расстояние: $it км",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
