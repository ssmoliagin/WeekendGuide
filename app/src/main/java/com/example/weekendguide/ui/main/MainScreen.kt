package com.example.weekendguide.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.Constants
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.ui.components.FiltersButtons
import com.example.weekendguide.ui.components.LoadingScreen
import com.example.weekendguide.ui.components.LocationPanel
import com.example.weekendguide.ui.components.NavigationBar
import com.example.weekendguide.ui.components.TopAppBar
import com.example.weekendguide.ui.filters.FiltersPanel
import com.example.weekendguide.ui.list.ListPOIScreen
import com.example.weekendguide.ui.map.MapScreen
import com.example.weekendguide.ui.poi.POIFullScreen
import com.example.weekendguide.ui.profile.ProfileScreen
import com.example.weekendguide.ui.statistics.StatisticsScreen
import com.example.weekendguide.ui.store.PoiStoreScreen
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    loginViewModel: LoginViewModel,
    themeViewModel: ThemeViewModel,
    translateViewModel: TranslateViewModel,
    locationViewModel: LocationViewModel,
    pointsViewModel: PointsViewModel,
    context: Context = LocalContext.current,
    onLoggedOut: () -> Unit,
) {
    //состояние окон
    var showMapScreen by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }
    var showStatisticsScreen by remember { mutableStateOf(false) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var showPOIInMap by remember { mutableStateOf(false) }
    var showListPOIScreen by remember { mutableStateOf(false) }
    var showPOIStoreScreen by remember { mutableStateOf(false) }
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

    //обновление очков
    val currentGP by pointsViewModel.currentGP.collectAsState()
    val totalGP by pointsViewModel.totalGP.collectAsState()
    val spentGP by pointsViewModel.spentGP.collectAsState()

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
        val poiViewModel: POIViewModel = viewModel(factory = POIViewModelFactory(context, reg, translateViewModel))

        val poiList by poiViewModel.poiList.collectAsState()

        val visitedPoiIds by poiViewModel.visitedPoiIds.collectAsState() //посещенные пои
        val favoriteIds by poiViewModel.favoriteIds.collectAsState() //  ИЗБРАННЫЕ ПОИ
        val onFavoriteClick: (String) -> Unit = { poiId ->
            poiViewModel.toggleFavorite(poiId)
        }

        //ФИЛЬТРАЦИЯ
        //читаем все типы точек
        val allTypes by poiViewModel.allTypes.collectAsState()

        // — правильная инициализация selectedTypes
        var selectedTypes by remember(allTypes) {
            mutableStateOf(if (allTypes.isNotEmpty()) allTypes else emptyList())
        }

        // переключение типа
        val onTypeToggle: (String) -> Unit = { type ->
            selectedTypes = if (type in selectedTypes) {
                selectedTypes - type
            } else {
                selectedTypes + type
            }
        }

        //радиус поиска точек
        var selectedRadius by remember { mutableStateOf("200км") }
        val radiusValue = when (selectedRadius) {
            "20км" -> 20
            "50км" -> 50
            "100км" -> 100
            "200км" -> 200
            "∞" -> Int.MAX_VALUE
            else -> 200
        }

        // Фильтрация POI по радиусу и типам
        val filteredPOIList = remember(poiList, userLocation, selectedRadius, selectedTypes, allTypes) {
            if (allTypes.isEmpty() || selectedTypes.isEmpty()) return@remember emptyList()

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

        //очищаем фильтры и окна
        fun resetFiltersUndScreens() {
            selectedTypes = allTypes
            selectedRadius = "200км"
            showOnlyVisited = false
            showOnlyFavorites = false

            showListPOIScreen = false
            showStatisticsScreen = false
            showMapScreen = false
            showProfileScreen = false
            showPOIStoreScreen = false
        }

        // --- НАВИГАЦИЯ ---

        //показ шапки
        @Composable
        fun showTopAppBar ()
        {
            TopAppBar(
                currentGP = currentGP, // ➕ добавили
                onItemSelected = { selectedItem = it }, // выделяем кнопку меню меню
                topBarTitle =
                    if (showListPOIScreen) {
                    if (showOnlyFavorites)"favorites"
                    else  "${finalPOIList.size} мест рядом с $currentCity"
                }
                    else if (showStatisticsScreen) "statistic"
                    else if (showProfileScreen) "profile"
                    else "main",
            onDismiss = {resetFiltersUndScreens()},
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
                    showListPOIScreen = true
                },
                onOpenProfile = {
                    showOnlyVisited = true
                    selectedRadius = "∞"
                    showProfileScreen = true
                                },
                onOpenStatistics = {
                    showOnlyVisited = true
                    selectedRadius = "∞"
                    showStatisticsScreen = true
                },
                onDismiss = {resetFiltersUndScreens()}
            )}

        //показ панель локации
        @Composable
        fun showLocationPanel() {
            LocationPanel(
                onShowScreenType =
                    if (showMapScreen) "map"
                    else "main",
                onLocationSelected = { city, latLng ->
                    // Распаковываем lat и lng
                    val (lat, lng) = latLng
                    locationViewModel.setManualLocation(city, lat, lng)
                },
                onRequestGPS = onRequestLocationChange,
                userCurrentCity = currentCity,
                onDismiss = {resetFiltersUndScreens()},
            )}

        //показ кнопки фильтры
        @Composable
        fun showFiltersButtons() {
            FiltersButtons(
                onShowScreenType =
                    if (showMapScreen) "map"
                    else "list",
                userCurrentCity = currentCity,
                onRequestGPS = onRequestLocationChange,
                selectedRadius = selectedRadius,
                onRadiusChange = { selectedRadius = it },
                onOpenMapScreen = { showMapScreen = true },
                onOpenListScreen = {showListPOIScreen = true},
                onOpenFilters = { showFiltersPanel = true },
                onDismiss = { showMapScreen = false },
            )
        }

        if (showMapScreen) {
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
        } else if (showListPOIScreen) {
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
                onOpenListScreen = {showListPOIScreen = true},
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
                translateViewModel = translateViewModel,
                )
        }

        //магазин
        if (showPOIStoreScreen) {
            PoiStoreScreen(
                isInitialSelection = false,
                translateViewModel = translateViewModel,
                pointsViewModel = pointsViewModel,
                onRegionChosen = {
                    // Можно обновить UI или показать Snackbar
                    showPOIStoreScreen = false
                },
                onDismiss = {
                    showPOIStoreScreen = false
                }
            )
        }

        // Экран Статистика
        if (showStatisticsScreen) {
            StatisticsScreen(
                totalGP = totalGP,
                currentGP = currentGP,
                spentGP = spentGP,
                userPOIList = finalPOIList,
                totalPOIList = poiList,
                allTypes = allTypes,
                showNavigationBar = { showNavigationBar() },
                showTopAppBar = { showTopAppBar () },
                pointsViewModel = pointsViewModel,
                translateViewModel = translateViewModel,
            )
        }

        // ✅ Экран Профиль
        if(showProfileScreen) {
            ProfileScreen(
                userPOIList = finalPOIList,
                totalPOIList = poiList,
                showNavigationBar = { showNavigationBar() },
                showTopAppBar = { showTopAppBar () },
                onLoggedOut = onLoggedOut,
                themeViewModel = themeViewModel,
                loginViewModel = loginViewModel,
                translateViewModel = translateViewModel,
                onOpenStore = {
                    showProfileScreen = false
                    showPOIStoreScreen = true}
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
                    onToggleShowVisited = { showVisited = !showVisited },
                    translateViewModel = translateViewModel,
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
                    pointsViewModel = pointsViewModel,
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
                pointsViewModel = pointsViewModel,
            )
        }
        
    } ?: LoadingScreen()
}