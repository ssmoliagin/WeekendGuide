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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.preferences.UserSettings
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import com.example.weekendguide.viewmodel.ViewModelFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.material.icons.filled.* // Это нужно!
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.mutableStateMapOf
import com.example.weekendguide.viewmodel.GPViewModel
import com.example.weekendguide.viewmodel.RegionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context = LocalContext.current) {
    //состояние окон
    var showMap by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showPOIInMap by remember { mutableStateOf(false) }
    var showListPoi by remember { mutableStateOf(false) }
    var onSortPOI by remember { mutableStateOf(false) }
    var showFullPOI by remember { mutableStateOf(false) }
    var showOnlyFavorites by remember { mutableStateOf(false) } //скрытый фильтр ТОЛЬКО избранные
    var showOnlyVisited by remember { mutableStateOf(false) } // скрытый фильтр ТОЛЬКО посещенные
    var showVisited by remember { mutableStateOf(true) } // показать посещенные

    var selectedItem by remember { mutableStateOf("main") }// 🔹 Состояние выбранного пункта меню
    var selectedPOI by remember { mutableStateOf<POI?>(null) }

    val prefs = UserPreferences(context)
    val region by produceState<Region?>(initialValue = null) {
        value = prefs.getHomeRegion()
    }

    val regionViewModel: RegionViewModel = viewModel()

    val locationViewModel: LocationViewModel = viewModel(
        key = "LocationViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    val gpViewModel: GPViewModel = viewModel(
        key = "GPViewModel",
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    //обновление очков
    val currentGP by gpViewModel.currentGP.collectAsState()
    val totalGP by gpViewModel.totalGP.collectAsState()
    val spentGP by gpViewModel.spentGP.collectAsState()

    // --- ОПРЕДЕЛЯЕМ ЛОКАЦИЮ ---
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

    var onRequestLocationChange = {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                locationViewModel.detectLocationFromGPS()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

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

    // --- ОСНОВНАЯ ЛОГИКА ---
    region?.let { reg ->
        val poiViewModel: POIViewModel = viewModel(factory = POIViewModelFactory(context, reg))
        val poiList by poiViewModel.poiList.collectAsState()
        val visitedPoiIds by poiViewModel.visitedPoiIds.collectAsState() //посещенные пои
        val favoriteIds by poiViewModel.favoriteIds.collectAsState() //  ИЗБРАННЫЕ ПОИ
        val onFavoriteClick: (String) -> Unit = { poiId ->
            poiViewModel.toggleFavorite(poiId)
        }

        /* //старый метод если все ок, удалить
        val onCheckpointClick: (String) -> Unit = { poiId ->
            poiViewModel.markPoiVisited(poiId)
        }
         */

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

        //определение отфильтрованых пои
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

        //один тип
        val onSelectSingleType: (String) -> Unit = { type ->
            selectedTypes = listOf(type)
        }

    //сортировка
    // определение итогового списка POI
        val finalPOIList = remember(
            filteredPOIList,
            onSortPOI,
            showOnlyFavorites,
            favoriteIds,
            showVisited,
            showOnlyVisited,
            visitedPoiIds
        ) {
            var baseList = filteredPOIList

            // 🔹 Только посещённые → фильтруем по visited
            baseList = when {
                showOnlyVisited -> baseList.filter { poi -> visitedPoiIds.contains(poi.id) }
                !showVisited -> baseList.filterNot { poi -> visitedPoiIds.contains(poi.id) }
                else -> baseList
            }

            // ⭐ Избранные
            if (showOnlyFavorites) {
                baseList = baseList.filter { poi -> favoriteIds.contains(poi.id) }
            }

            // 📍 Сортировка по расстоянию
            if (onSortPOI) {
                userLocation?.let { (lat, lon) ->
                    baseList.sortedBy { poi ->
                        val result = FloatArray(1)
                        Location.distanceBetween(lat, lon, poi.lat, poi.lng, result)
                        result[0]
                    }
                } ?: baseList
            } else {
                baseList
            }
        }

        fun resetFilters() {
            selectedTypes = allTypes
            selectedRadius = "200км"
            showOnlyVisited = false
            showOnlyFavorites = false

            showListPoi = false
            showStatistics = false
            showMap = false
        }

        // --- НАВИГАЦИЯ ---

        //показ шапки
        @Composable
        fun showTopAppBar ()
        {
            TopAppBar(
                currentGP = currentGP, // ➕ добавили
                onItemSelected = { selectedItem = it }, // выделяем кнопку меню меню
                topBarTitle = if (showListPoi) {
                    if (showOnlyFavorites)"favorites"
                    else if (showStatistics) "statistic"
                    else  "${finalPOIList.size} мест рядом с $currentCity"
                } else if (showStatistics) "statistic"
                        else "main",
            onDismiss = {resetFilters()},
        ) }

        //показ меню
        @Composable
        fun showNavigationBar() {
            NavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }, // выделяем кнопку меню меню
                onShowFavoritesList = {
                    showOnlyFavorites = true
                    selectedRadius = "∞"
                    showListPoi = true
                },
                onOpenProfile = { showProfile = true },
                onOpenStatistics = {
                    showOnlyVisited = true
                    selectedRadius = "∞"
                    showStatistics = true
                },
                onDismiss = {resetFilters()}
            )}

        //показ панель локации
        @Composable
        fun showLocationPanel() {
            LocationPanel(
                onShowScreenType =
                    if (showMap) "map"
                    else "main",
                onLocationSelected = { city, latLng ->
                    // Распаковываем lat и lng
                    val (lat, lng) = latLng
                    locationViewModel.setManualLocation(city, lat, lng)
                },
                onRequestGPS = onRequestLocationChange,
                userCurrentCity = currentCity,
                onDismiss = {resetFilters()},
            )}

        //показ кнопки фильтры
        @Composable
        fun showFiltersButtons() {
            FiltersButtons(
                onShowScreenType =
                    if (showMap) "map"
                    else "list",
                userCurrentCity = currentCity,
                onRequestGPS = onRequestLocationChange,
                selectedRadius = selectedRadius,
                onRadiusChange = { selectedRadius = it },
                onOpenMapScreen = { showMap = true },
                onOpenListScreen = {showListPoi = true},
                onOpenFilters = { showFiltersPanel = true },
                onDismiss = { showMap = false },
            )
        }



        if (showMap) {
            MapScreen(
                userPOIList = finalPOIList,
                userLocation = userLocation,
                selectedRadius = selectedRadius,
                onSelectPOI = { poi -> selectedPOI = poi },
                onOpenPOIinMap = {showPOIInMap = true},
                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                showLocationPanel = { showLocationPanel() },
                showFiltersButtons = { showFiltersButtons() },
            )
        } else if (showListPoi) {
            ListPOIScreen(
                userPOIList = finalPOIList,
                userLocation = userLocation,
                userCurrentCity = currentCity,
                onSelectPOI = { poi -> selectedPOI = poi },
                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                onFavoriteClick = onFavoriteClick,
                onPOIClick = {showFullPOI = true},
                showTopAppBar = { showTopAppBar () },
                showNavigationBar = { showNavigationBar() },
                showFiltersButtons = { showFiltersButtons() },
            )
        }

        else {
            MainContent(
                userPOIList = finalPOIList,
                userLocation = userLocation,
                userCurrentCity = currentCity,
                onOpenListScreen = {showListPoi = true},
                onSelectSingleType = onSelectSingleType,
                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                onFavoriteClick = onFavoriteClick,
                onPOIClick = {showFullPOI = true},
                onSelectPOI = { poi -> selectedPOI = poi },
                showNavigationBar = { showNavigationBar() },
                showTopAppBar = { showTopAppBar () },
                showLocationPanel = { showLocationPanel() },
                showFiltersButtons = { showFiltersButtons() },
                )
        }

        // Экран Статистика
        if (showStatistics) {
            StatisticsScreen(
                totalGP = totalGP,
                currentGP = currentGP,
                spentGP = spentGP,
                userPOIList = finalPOIList,
                totalPOIList = poiList,
                allTypes = allTypes,
                showNavigationBar = { showNavigationBar() },
                showTopAppBar = { showTopAppBar () },
                gpViewModel = gpViewModel,
            )
        }

        // ✅ Панель Фильтры
        if (showFiltersPanel) {
            ModalBottomSheet(
                onDismissRequest = {showFiltersPanel = false},
                sheetState = rememberModalBottomSheetState()
            ) {
                FiltersPanel(
                    selectedRadius = selectedRadius,
                    onRadiusChange = { selectedRadius = it },
                    allTypes = poiList.map { it.type }.distinct(),
                    selectedTypes = selectedTypes,
                    onTypeToggle = onTypeToggle,
                    onSelectAllTypes = { selectedTypes = allTypes },
                    onClearAllTypes = { selectedTypes = emptyList() },
                    showVisited = showVisited,
                    onToggleShowVisited = { showVisited = !showVisited }
                )
            }
        }

        // ✅ Панель Профиль
        if(showProfile) {
            ModalBottomSheet(
                onDismissRequest = {showProfile = false},
                sheetState = rememberModalBottomSheetState()
            ) {
                ProfilePanel (
                    onDismiss = { showProfile = false}
                )
            }
        }

        // ✅ POI на Карте
        val poi = selectedPOI
        if (showPOIInMap && poi != null && !showFullPOI) {
            ModalBottomSheet(
                onDismissRequest = {showPOIInMap = false},
                sheetState = rememberModalBottomSheetState()
            ) {
                POIFullScreen (
                    poi = poi,
                    isFavorite = favoriteIds.contains(poi.id),
                    onFavoriteClick = { poiViewModel.toggleFavorite(poi.id) },
                    isVisited = visitedPoiIds.contains(poi.id),
                    userLocation = userLocation,
                    userCurrentCity = currentCity,
                    onDismiss = {
                        selectedPOI = null
                        showFullPOI = false
                    },
                    poiViewModel = poiViewModel,
                    gpViewModel = gpViewModel,
                )
            }
        }

        //✅ POI на весь экран
        if (showFullPOI && poi != null) {
            POIFullScreen (
                poi = poi,
                isFavorite = favoriteIds.contains(poi.id),
                onFavoriteClick = { poiViewModel.toggleFavorite(poi.id) },
                isVisited = visitedPoiIds.contains(poi.id),
                userLocation = userLocation,
                userCurrentCity = currentCity,
                onDismiss = {
                    selectedPOI = null
                    showFullPOI = false
                },
                poiViewModel = poiViewModel,
                gpViewModel = gpViewModel,
            )
        }
        
    } ?: LoadingScreen()
}

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
    ) {
    
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp // ширина экрана

    Scaffold(

        //ШАПКА
        topBar = {
            showTopAppBar()
        },
        //НИЖНЕЕ МЕНЮ
        bottomBar = {
            showNavigationBar()
        }

        // ОСНОВНОЙ ЭКРАН
    ) { paddingValues ->

        Column (
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            //Поле выбор локации
            showLocationPanel()

           Spacer(modifier = Modifier.height(4.dp))

            // кнопки фильтры
            showFiltersButtons()


            Spacer(modifier = Modifier.height(16.dp))

            // Основной список
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
                            text = "$count мест рядом с ${userCurrentCity ?: "вами"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Показать все",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                onOpenListScreen()       // и открываем экран со списком
                            }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(displayedPOIs) { poi ->
                            Box(
                                modifier = Modifier
                                    .width(screenWidth - 32.dp)
                            ) {
                                POICard(
                                    poi = poi,
                                    isFavorite = isFavorite(poi),
                                    isVisited = isVisited(poi),
                                    onFavoriteClick = { onFavoriteClick(poi.id) },
                                    userLocation = userLocation,
                                    userCurrentCity = userCurrentCity,
                                    cardType = "list",
                                    onClick = onPOIClick,
                                    onSelectPOI = onSelectPOI
                                )
                            }

                        }
                    }
                }

                //группировки по типам
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
                                text = "$count ${type.replaceFirstChar { it.uppercase() }} рядом с ${userCurrentCity ?: "вами"}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Показать все",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onSelectSingleType(type) // фильтруем только по одному типу
                                    onOpenListScreen()       // и открываем экран со списком
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
                                    onSelectPOI = onSelectPOI
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    selectedRadius: String,
    onSelectPOI: (POI) -> Unit,
    onOpenPOIinMap: () -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    showLocationPanel: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,
    ) {

    val radiusValue = when (selectedRadius) {
        "20км" -> 20_000.0
        "50км" -> 50_000.0
        "100км" -> 100_000.0
        "200км" -> 200_000.0
        "∞" -> 0.0
        else -> 200_000.0
    }
    val zoom = when (selectedRadius) {
        "20км" -> 10f
        "50км" -> 9f
        "100км" -> 8f
        else -> 7f
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation?.let { LatLng(it.first, it.second) } ?: LatLng(51.1657, 10.4515),
            zoom
        )
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
             //   properties = MapProperties(isMyLocationEnabled = true) // Кнопка где я
            ) {


                userPOIList.forEach { poi ->
                    val markerColor = when {
                        isVisited(poi) -> BitmapDescriptorFactory.HUE_GREEN
                        isFavorite(poi) -> BitmapDescriptorFactory.HUE_YELLOW
                        else -> BitmapDescriptorFactory.HUE_RED
                    }

                    val icon = BitmapDescriptorFactory.defaultMarker(markerColor)

                    Marker(
                        state = MarkerState(position = LatLng(poi.lat, poi.lng)),
                        title = poi.title,
                        icon = icon,
                        onClick = {
                            onSelectPOI(poi)
                            onOpenPOIinMap()
                            true
                        }
                    )
                }

                userLocation?.let {
                    Circle(
                        center = LatLng(it.first, it.second),
                        radius = radiusValue,
                        fillColor = Color.Blue.copy(alpha = 0.15f),
                        strokeColor = Color.Blue,
                        strokeWidth = 2f
                    )
                }
            }

            Column (
                modifier = Modifier
                    .padding(top = 40.dp, start = 4.dp, end = 4.dp)
                    .fillMaxSize()
            ) {
                //Поле выбор локации
                showLocationPanel()

                Spacer(modifier = Modifier.height(4.dp))

                // кнопки фильтры
                showFiltersButtons()
            }
        }
}

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
                        onSelectPOI = onSelectPOI
                    )
                }
            }
        }
    }
}

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
) {
    val radiusValues = listOf("20", "50", "100", "200", "∞")
    val radiusSliderPosition = radiusValues.indexOfFirst {
        it.removeSuffix("км") == selectedRadius.removeSuffix("км")
    }.coerceAtLeast(0)

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
        "extreme" to Icons.Default.DownhillSkiing
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
                                        text = type,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(type)
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



@Composable
fun ProfilePanel(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { UserPreferences(context) }
    val user = FirebaseAuth.getInstance().currentUser

    var userSettings by remember { mutableStateOf<UserSettings?>(null) }

    LaunchedEffect(Unit) {
        userSettings = withContext(Dispatchers.IO) {
            preferences.getAll()
        }
    }

    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Профиль", style = MaterialTheme.typography.headlineSmall)

            user?.email?.let {
                ProfileRow(label = "Email", value = it)
            }
            ProfileRow(
                label = "currentGP",
                value = userSettings?.currentGP.toString() ?: "-"
            )

            ProfileRow(
                label = "totalGP",
                value = userSettings?.totalGP.toString() ?: "-"
            )

            ProfileRow(
                label = "Язык интерфейса",
                value = userSettings?.language ?: "-"
            )

            ProfileRow(
                label = "Регионы",
                value = userSettings?.purchasedRegions?.joinToString(", ") ?: "-"
            )

            ProfileRow(
                label = "Избранное",
                value = userSettings?.favoritePoiIds?.joinToString(", ") ?: "-"
            )

            ProfileRow(
                label = "Посещенное",
                value = userSettings?.visitedPoiIds?.joinToString(", ") ?: "-"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        FirebaseAuth.getInstance().signOut()
                     //   context.dataStore.edit { it.clear() }
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
fun POICard(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,

    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    cardType: String? = "list",
    onClick: () -> Unit,
    onSelectPOI: (POI) -> Unit,
    ) {

    val cardModifier: Modifier
    val imageModifier: Modifier
    val isImageLeft: Boolean

    when (cardType) {
        "mini" -> {
            cardModifier = Modifier
                .width(200.dp)
                .height(300.dp)
                .clickable {
                    onSelectPOI(poi)
                    onClick()
                }
            imageModifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
            isImageLeft = false
        }
        else -> {  //"list"
            cardModifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clickable {
                    onSelectPOI(poi)
                    onClick()
                }
            imageModifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
            isImageLeft = false
        }
    }

    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = imageModifier) {
                    poi.imageUrl?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    //посещенные
                    if (isVisited) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp),
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Посещенные",
                            tint = Color.Green
                        )
                    }

                    //избранные
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.White.copy(alpha = 0.6f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Избранные",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = poi.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = poi.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    distanceKm?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$it км от $userCurrentCity",
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
fun POIFullScreen(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,

    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    onDismiss: () -> Unit,
    poiViewModel: POIViewModel,
    gpViewModel: GPViewModel
) {

    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }

    val wikiDescription by poiViewModel.wikiDescription.collectAsState()

    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val location by locationViewModel.location.collectAsState()

    val prefs = remember { UserPreferences(context) }


    LaunchedEffect(poi.title) {
        poiViewModel.loadWikipediaDescription(poi.title)
    }
/*
    fun handleCheckpointClick() {
        coroutineScope.launch {
            try {
                val oldLocation = locationViewModel.location.value

                // Запрашиваем GPS-локацию
                locationViewModel.detectLocationFromGPS()

                // Ждём, пока локация изменится
                val newLocation = locationViewModel.location
                    .filterNotNull()
                    .dropWhile { it == oldLocation }
                    .first()

                // Считаем дистанцию
                val result = FloatArray(1)
                Location.distanceBetween(
                    newLocation.first,
                    newLocation.second,
                    poi.lat,
                    poi.lng,
                    result
                )
                val distanceMeters = result[0]

                if (distanceMeters < 20000000) {  //дистанция в метрах
                    poiViewModel.markPoiVisited(poi.id)
                    gpViewModel.addGP(100)
                    Toast.makeText(context, "+100 GP за посещение!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Вы слишком далеко от точки", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка при определении GPS", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

 */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(poi.title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        poi.imageUrl?.let { imageUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Левая верхняя иконка "Посещено" — всегда видна
                        if(isVisited) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Посещенно",
                                tint = Color.Green
                            )
                        }

                        // Правая верхняя иконка "Избранное"
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Избранное",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }

                item {
                    // Рейтинг
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < 5) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Рейтинг: 5.0",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item {
                    // Описание
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = poi.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = wikiDescription ?: poi.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 20.sp
                        )
                        distanceKm?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$it км от $userCurrentCity",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item {
                    // Карта
                    Text(
                        text = "На карте:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp),
                        cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(poi.lat, poi.lng), 14f
                            )
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(poi.lat, poi.lng)),
                            title = poi.title
                        )
                    }
                }

                item {
                    // Отзывы
                    Text(
                        text = "Отзывы",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("«Отличное место! Очень рекомендую!»", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Оставьте отзыв...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { /* TODO: сохранить отзыв */ },
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.End)
                        ) {
                            Text("Отправить")
                        }
                    }
                }
            }

            // Кнопка чекпоинта
            var isChecking by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    isChecking = true
                    coroutineScope.launch {
                        gpViewModel.checkAndAwardGPForPOI(poi, locationViewModel) { success ->
                            if (success) {
                                poiViewModel.markPoiVisited(poi.id)
                                Toast.makeText(context, "+100 GP за посещение!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Вы слишком далеко от точки", Toast.LENGTH_SHORT).show()
                            }
                            isChecking = false
                        }
                    }
                },
                enabled = !isVisited && !isChecking,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVisited) Color.Gray else Color.Green
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when {
                        isVisited -> "Уже посещено"
                        isChecking -> "Проверка..."
                        else -> "Чекпоинт"
                    },
                    color = Color.White
                )
            }
        }
    }
}

// ЭЛЕМЕНТЫ ЭКРАНА
//шапка
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar (
    currentGP: Int,
    onItemSelected: (String) -> Unit,
    topBarTitle: String,
    onDismiss: () -> Unit
) {
    val sound = LocalView.current

    /* old
      val context = LocalContext.current
      val prefs = UserPreferences(context)
      var value by remember { mutableStateOf<UserSettings?>(null) }
      LaunchedEffect(Unit) {
          value = prefs.getAll()
      }
       */

    val title = when (topBarTitle) {
        "main" -> "Weekend Guide"
        "favorites" -> "Избранное"
        "statistic" -> "Достижения"
        else -> topBarTitle
    }

    TopAppBar(
        title = { Text(title, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("$currentGP 🏆", color = Color.White)
            }
        },
        navigationIcon = {
            if (topBarTitle != "main") {
                IconButton(onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    onItemSelected("main")
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onShowFavoritesList: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenStatistics: () -> Unit,
    onDismiss: () -> Unit
) {
    val sound = LocalView.current

    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == "main",
            onClick = {
                onItemSelected("main")
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onDismiss()
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            label = { Text("Поиск") }
        )

        NavigationBarItem(
            selected = selectedItem == "favorites",
            onClick = {
                onItemSelected("favorites")
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onDismiss()
                onShowFavoritesList()
            },
            icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Избранное") },
            label = { Text("Избранное") }
        )

        NavigationBarItem(
            selected = selectedItem == "statistics",
            onClick = {
                onItemSelected("statistics")
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onDismiss()
                onOpenStatistics()
            },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Достижения") },
            label = { Text("Достижения") }
        )

        NavigationBarItem(
            selected = selectedItem == "profile",
            onClick = {
                onItemSelected("profile")
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onOpenProfile()
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label = { Text("Профиль") }
        )
    }
}



//Панель ввода местоположения
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPanel(
    userCurrentCity: String?,
    onLocationSelected: (String, Pair<Double, Double>) -> Unit,
    onRequestGPS: () -> Unit,
    onShowScreenType: String?,
    onDismiss: () -> Unit
)
{
    val sound = LocalView.current
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var query by remember { mutableStateOf("") }
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
        } else {
            suggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(userCurrentCity ?:"Город или адрес") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),

            leadingIcon = {
                if (onShowScreenType == "map") {
                    // 🔙 Кнопка "Назад"
                    IconButton(onClick = {
                        sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    onRequestGPS()
                    suggestions = emptyList() //clear
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Определить по GPS")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(suggestions) { prediction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
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
                                    suggestions = emptyList() //clear
                                }
                        },
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = prediction.getFullText(null).toString(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

//кнопки фильтры
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersButtons(
    userCurrentCity: String?,
    onRequestGPS: () -> Unit,
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    onOpenMapScreen: () -> Unit,
    onOpenListScreen: () -> Unit,
    onOpenFilters: () -> Unit,
    onShowScreenType: String? = null,
    onDismiss: () -> Unit,
) {
    val sound = LocalView.current

    val buttonHeight = 30.dp
    var radiusExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        val commonButtonColors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White // Белый фон для всех кнопок
        )

        if (onShowScreenType == "map") {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    onOpenListScreen()
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Список", fontSize = 13.sp, maxLines = 1)
            }
        } else {
            OutlinedButton(
                onClick = {
                    sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    if (userCurrentCity == null) {
                        onRequestGPS()
                    } else {
                        onOpenMapScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("На карте", fontSize = 13.sp, maxLines = 1)
            }
        }

        // 🎯 Кнопка с выпадающим списком радиуса
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { radiusExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = commonButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(" + $selectedRadius", fontSize = 13.sp, maxLines = 1)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // ⬇ Выпадающее меню
            DropdownMenu(
                expanded = radiusExpanded,
                onDismissRequest = { radiusExpanded = false },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.TopCenter)
            ) {
                listOf("20км", "50км", "100км", "200км", "∞").forEach { radius ->
                    DropdownMenuItem(
                        text = { Text(radius) },
                        onClick = {
                            sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                            onRadiusChange(radius)
                            radiusExpanded = false
                        }
                    )
                }
            }
        }

        // ⚙️ Кнопка "Фильтр"
        OutlinedButton(
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onOpenFilters()
            },
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = commonButtonColors
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Фильтр", fontSize = 13.sp, maxLines = 1)
        }
    }
}

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
    gpViewModel: GPViewModel
) {
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
        "extreme" to Icons.Default.DownhillSkiing
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
                                text = "${type.replaceFirstChar { it.uppercaseChar() }} - $count",
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
                                        gpViewModel.addGP(1000)
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







@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


