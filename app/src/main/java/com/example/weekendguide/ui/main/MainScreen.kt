package com.example.weekendguide.ui.main

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import com.example.weekendguide.viewmodel.ViewModelFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context = LocalContext.current) {
    var showMap by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }

    val locationViewModel: LocationViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val prefs = UserPreferences(context)

    val region by produceState<Region?>(initialValue = null) {
        value = prefs.getHomeRegion()
    }
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

    // --- новое поле для ввода города ---
    var cityQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    val api = Constants.GOOGLE_MAP_API
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, api)
        }
        Places.createClient(context)
    }

    fun fetchPredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypeFilter(TypeFilter.CITIES)
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                Log.e("MainScreen", "Places API error", exception)
            }
    }

    region?.let { reg ->
        val viewModel: POIViewModel = viewModel(factory = POIViewModelFactory(context, reg))
        val poiList by viewModel.poiList.collectAsState()
        //val radiusOptions = listOf("20км", "50км", "100км", "200км", "∞")

        //ФИЛЬТРАЦИЯ
        var selectedRadius by remember { mutableStateOf("200км") }
        val allTypes = listOf("castle", "nature", "park", "funpark", "museum", "swimming", "hiking", "cycling", "zoo", "city-walk", "festival", "extreme")
        var selectedTypes by remember { mutableStateOf(allTypes) }

        val onTypeToggle: (String) -> Unit = { type ->
            selectedTypes = if (type in selectedTypes) {
                selectedTypes - type
            } else {
                selectedTypes + type
            }
        }

        val radiusValue = when (selectedRadius) {
            "20км" -> 20
            "50км" -> 50
            "100км" -> 100
            "200км" -> 200
            "∞" -> Int.MAX_VALUE
            else -> 200
        }

        val filteredPOIList = remember(poiList, userLocation, selectedRadius, selectedTypes.toList()) {
            val distanceFiltered = userLocation?.let { (lat, lon) ->
                poiList.filter { poi ->
                    val result = FloatArray(1)
                    Location.distanceBetween(lat, lon, poi.lat, poi.lng, result)
                    val distanceInKm = result[0] / 1000
                    distanceInKm <= radiusValue
                }
            } ?: poiList
            distanceFiltered.filter { poi -> selectedTypes.contains(poi.type) }
        }

        //НАВИГАЦИЯ
        if (showMap) {
            MapScreen(
                poiList = filteredPOIList,
                userLocation = userLocation,
                currentCity = currentCity,
                onRequestLocationChange = {
                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                            locationViewModel.detectLocationFromGPS()
                        }
                        else -> {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                },
                selectedRadius = selectedRadius,
                onRadiusChange = { selectedRadius = it },
                onOpenFilters = { showFiltersPanel = true }, // ✅
                filteredPOIList = filteredPOIList,  // <-- добавлено!
                onBack = {
                    showMap = false
                }
            )
        } else {
            MainContent(
                viewModel = viewModel,
                region = reg,
                userLocation = userLocation,
                currentCity = currentCity,
                onRequestLocationChange = {
                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
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
                },
                onNavigateToMapScreen = {
                    showMap = true
                },
                selectedRadius = selectedRadius,
                onRadiusChange = { selectedRadius = it },
                allTypes = allTypes,
                onTypeToggle = onTypeToggle,
                selectedTypes = selectedTypes,
                onOpenFilters = { showFiltersPanel = true }, // ✅
                filteredPOIList = filteredPOIList
            )
        }
        // ✅ Панель фильтров как BottomSheet
        if (showFiltersPanel) {
            ModalBottomSheet(
                onDismissRequest = {showFiltersPanel = false},
                sheetState = rememberModalBottomSheetState()
            ) {
                FiltersPanel(
                    selectedRadius = selectedRadius,
                    onRadiusChange = { selectedRadius = it },
                    allTypes = allTypes,
                    selectedTypes = selectedTypes,
                    onTypeToggle = onTypeToggle,
                    onDismiss = { showFiltersPanel = false }
                )
            }
        }
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
    onShowProfile: () -> Unit,
    onNavigateToMapScreen: () -> Unit,
    onRadiusChange: (String) -> Unit,
    onTypeToggle: (String) -> Unit, // <-- ДОБАВЛЕНО
    selectedRadius: String,
    allTypes: List<String>,
    selectedTypes: List<String>,
    onOpenFilters: () -> Unit, // ✅ новое
    filteredPOIList: List<POI>
) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    var randomPOI by remember { mutableStateOf<POI?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val radiusOptions = listOf("20км", "50км", "100км", "200км", "∞")


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
            /*
            Spacer(modifier = Modifier.height(8.dp))
//КНОПКА ФИЛЬТР
            Button(
                onClick = onOpenFilters,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Фильтры")
            }

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
                        onClick = { onRadiusChange(radius) }, // <-- используй onRadiusChange, а не локальный стейт
                        label = { Text(radius) }
                    )
                }
            }

             */


            // включение диалога выбора локации, перенести в маин
            var showLocationDialog by remember { mutableStateOf(false) }
            if (showLocationDialog) {
                LocationSelectorDialog(
                    onDismiss = { showLocationDialog = false },
                    onLocationSelected = { city, latLng ->
                        // Распаковываем lat и lng
                        val (lat, lng) = latLng
                        locationViewModel.setManualLocation(city, lat, lng)
                    },
                    onRequestGPS = onRequestLocationChange
                )
            }


// Поле текущего местоположения

            Row(
                modifier = Modifier
                    .padding(top = 16.dp, start = 5.dp, end = 5.dp)
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка LocationOn
                IconButton(onClick = {showLocationDialog = true}) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 📍 Центр панели — кликабельное поле
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { showLocationDialog = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = currentCity ?: "Искать рядом с...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (currentCity != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                // ⚙️ Кнопка фильтров
                IconButton(onClick = onOpenFilters) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Фильтры",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

                //

            /*
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // .clip(RoundedCornerShape(50)) // овал
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showLocationDialog = true } // клик снаружи
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = currentCity ?: "Определение местоположения...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (currentCity != null) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(20.dp)
                )
            }

             */

            Spacer(modifier = Modifier.height(8.dp))



            // Кнопка Показать на карте
            Button(
                onClick = {
                    if (currentCity == null) {
                        showLocationDialog = true
                    } else {
                        onNavigateToMapScreen()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Показать на карте")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    poiList: List<POI>,
    userLocation: Pair<Double, Double>?,
    currentCity: String?,
    onRequestLocationChange: () -> Unit,
    onBack: () -> Unit,
    onRadiusChange: (String) -> Unit,
    selectedRadius: String,
    onOpenFilters: () -> Unit, // ✅ новое
    filteredPOIList: List<POI> // <-- добавлено!
) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    var randomPOI by remember { mutableStateOf<POI?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val radiusOptions = listOf("20км", "50км", "100км", "200км", "∞")
    var showLocationDialog by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation?.let { LatLng(it.first, it.second) } ?: LatLng(51.1657, 10.4515),
            8f
        )
    }
    Scaffold(
        /*
        topBar = {
            TopAppBar(
                title = { Text("Карта", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                }
            )
        }
         */
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 📍 КАРТА
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
             //   properties = MapProperties(isMyLocationEnabled = true) // Кнопка где я
            ) {
                poiList.forEach { poi ->
                    Marker(
                        state = MarkerState(position = LatLng(poi.lat, poi.lng)),
                        title = poi.title,
                        snippet = poi.description
                    )
                }

                userLocation?.let {
                    Circle(
                        center = LatLng(it.first, it.second),
                        radius = 50.0,
                        fillColor = Color.Blue.copy(alpha = 0.2f),
                        strokeColor = Color.Blue
                    )
                }
            }

            // 🎯 ПАНЕЛЬ — единая "таблетка" с тремя элементами
            Row(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔙 Кнопка "Назад"
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 📍 Центр панели — кликабельное поле
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { showLocationDialog = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = currentCity ?: "Определение местоположения...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (currentCity != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // ⚙️ Кнопка фильтров
                IconButton(onClick = onOpenFilters) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Фильтры",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 📍 Диалог выбора местоположения
            if (showLocationDialog) {
                LocationSelectorDialog(
                    onDismiss = { showLocationDialog = false },
                    onLocationSelected = { city, latLng ->
                        val (lat, lng) = latLng
                        locationViewModel.setManualLocation(city, lat, lng)
                    },
                    onRequestGPS = onRequestLocationChange
                )
            }
        }
    }


    // 📍 Диалог
    if (showLocationDialog) {
        LocationSelectorDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { city, latLng ->
                val (lat, lng) = latLng
                locationViewModel.setManualLocation(city, lat, lng)
            },
            onRequestGPS = onRequestLocationChange
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LocationSelectorDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String, Pair<Double, Double>) -> Unit,
    onRequestGPS: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.length >= 3) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    suggestions = response.autocompletePredictions
                }
                .addOnFailureListener { e ->
                    suggestions = emptyList()
                    e.printStackTrace()
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите местоположение") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Город") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn {
                    items(suggestions) { prediction ->
                        Text(
                            prediction.getFullText(null).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val placeId = prediction.placeId
                                    val placeRequest = FetchPlaceRequest.builder(
                                        placeId,
                                        listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                                    ).build()

                                    placesClient.fetchPlace(placeRequest)
                                        .addOnSuccessListener { result ->
                                            val place = result.place
                                            val latLng = place.latLng
                                            if (latLng != null) {
                                                onLocationSelected(
                                                    place.name ?: "",
                                                    Pair(latLng.latitude, latLng.longitude)
                                                )
                                            }
                                            onDismiss()
                                        }
                                }
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onRequestGPS()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Определить по GPS")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersPanel(
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    allTypes: List<String>,
    selectedTypes: List<String>,
    onTypeToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val radiusValues = listOf("20", "50", "100", "200", "∞")
    val radiusSliderPosition = radiusValues.indexOfFirst { it.removeSuffix("км") == selectedRadius.removeSuffix("км") }
        .coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Дальность (км)", style = MaterialTheme.typography.titleMedium)

            // 🔘 Слайдер радиуса
            Slider(
                value = radiusSliderPosition.toFloat(),
                onValueChange = {
                    val selected = radiusValues[it.toInt()]
                    onRadiusChange(if (selected == "∞") selected else "${selected}км")
                },
                steps = radiusValues.size - 2,
                valueRange = 0f..(radiusValues.size - 1).toFloat()
            )

            Text(
                text = "Выбрано: $selectedRadius",
                modifier = Modifier.padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )

            // ✅ Чекбоксы по типам
            Text("Типы мест", style = MaterialTheme.typography.titleMedium)
            Column {
                allTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onTypeToggle(type) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = type in selectedTypes,
                            onCheckedChange = { onTypeToggle(type) }
                        )
                        Text(type, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // ❌ Закрыть
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Закрыть")
            }
        }
    }
}

