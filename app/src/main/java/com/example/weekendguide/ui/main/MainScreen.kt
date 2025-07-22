package com.example.weekendguide.ui.main

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.Constants
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.ui.components.FiltersButtons
import com.example.weekendguide.ui.components.LoadingOverlay
import com.example.weekendguide.ui.components.LocationPanel
import com.example.weekendguide.ui.components.NavigationBar
import com.example.weekendguide.ui.components.StoreBanner
import com.example.weekendguide.ui.components.TopAppBar
import com.example.weekendguide.ui.filters.FiltersPanel
import com.example.weekendguide.ui.list.ListPOIScreen
import com.example.weekendguide.ui.map.MapScreen
import com.example.weekendguide.ui.poi.POIFullScreen
import com.example.weekendguide.ui.profile.ProfileScreen
import com.example.weekendguide.ui.statistics.StatisticsScreen
import com.example.weekendguide.ui.store.StoreScreen
import com.example.weekendguide.viewmodel.LeaderboardViewModel
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.MainStateViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.POIViewModelFactory
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.ProfileViewModel
import com.example.weekendguide.viewmodel.StatisticsViewModel
import com.example.weekendguide.viewmodel.StatisticsViewModelFactory
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.example.weekendguide.viewmodel.ViewModelFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    app: Application,
    loginViewModel: LoginViewModel,
    themeViewModel: ThemeViewModel,
    translateViewModel: TranslateViewModel,
    locationViewModel: LocationViewModel,
    pointsViewModel: PointsViewModel,
    onLoggedOut: () -> Unit,
    userPreferences: UserPreferences,
    dataRepository: DataRepositoryImpl,
    userRemoteDataSource: UserRemoteDataSource,
    leaderboardViewModel: LeaderboardViewModel
) {

    var showMapScreen by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }
    var showStatisticsScreen by remember { mutableStateOf(false) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var showPOIInMap by remember { mutableStateOf(false) }
    var showListPOIScreen by remember { mutableStateOf(false) }
    var showPOIStoreScreen by remember { mutableStateOf(false) }
    var onSortPOI by remember { mutableStateOf(false) }
    var showFullPOI by remember { mutableStateOf(false) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showOnlyVisited by remember { mutableStateOf(false) }
    var showVisited by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf("main") }
    var selectedPOI by remember { mutableStateOf<POI?>(null) }
    val currentLanguage by translateViewModel.language.collectAsState()
    val currentGP by pointsViewModel.currentGP.collectAsState()
    val totalGP by pointsViewModel.totalGP.collectAsState()
    val spentGP by pointsViewModel.spentGP.collectAsState()
    val isPremium by pointsViewModel.premium.collectAsState()
    val userLocation by locationViewModel.location.collectAsState()
    val currentCity by locationViewModel.currentCity.collectAsState()

    val mainStateViewModel: MainStateViewModel = viewModel(
        key = "MainStateViewModel",
        factory = ViewModelFactory(app, userPreferences, userRemoteDataSource )
    )

    val regions by mainStateViewModel.regions.collectAsState()

    val profileViewModel: ProfileViewModel = viewModel(
        key = "ProfileViewModel",
        factory = ViewModelFactory(app, userPreferences, userRemoteDataSource)
    )
    val currentUnits by profileViewModel.units.collectAsState()

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(userPreferences, userRemoteDataSource)
    )

    val typeIcons = mapOf(
        "architecture" to Icons.Default.Castle,
        "nature" to Icons.Default.Forest,
        "museum" to Icons.Default.Museum,
        "fun" to Icons.Default.Attractions,
        "zoo" to Icons.Default.Pets,
        "water" to Icons.Default.Pool,
        "active" to Icons.Default.DownhillSkiing,
        "hiking" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "cycling" to Icons.AutoMirrored.Filled.DirectionsBike,
        "culture" to Icons.Default.TheaterComedy,
    )

    // --- LOCATION ---

    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                locationViewModel.detectLocationFromGPS()
            } else {
                Toast.makeText(app, LocalizerUI.t("permission", currentLanguage), Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun onRequestLocationChange()
    {
        when {
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                isLoading = true
                coroutineScope.launch {
                    locationViewModel.detectLocationFromGPS()
                    isLoading = false
                }
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
            Places.initialize(app, api)
        }
        Places.createClient(app)
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

    // --- REFRESH VIEWMODELS ---
    LaunchedEffect(Unit) {
        locationViewModel.loadSavedLocation()
        if (currentCity.isNullOrEmpty()) onRequestLocationChange()

        themeViewModel.loadTheme()
        translateViewModel.refreshLang()
        pointsViewModel.refreshGP()
    }

    // --- CORE ---
    regions.let { reg ->

        if (regions.isNotEmpty()) {
            val poiViewModel: POIViewModel = remember(regions) {
                POIViewModelFactory(
                    context = app,
                    region = regions,
                    translateViewModel = translateViewModel,
                    dataRepository = dataRepository,
                    userPreferences = userPreferences,
                    userRemote = userRemoteDataSource
                ).create(POIViewModel::class.java)
            }

            val poiList by poiViewModel.poiList.collectAsState()
            val allReviews by poiViewModel.reviews.collectAsState()
            val reviewsList = allReviews.values.flatten()

            val visitedPoiIds by poiViewModel.visitedPoiIds.collectAsState()
            val favoriteIds by poiViewModel.favoriteIds.collectAsState()
            val onFavoriteClick: (String) -> Unit = { poiId ->
                poiViewModel.toggleFavorite(poiId)
            }

            // --- TYPES and FILTERS ---

            //Types
            val allTypes by poiViewModel.allTypes.collectAsState()

            var selectedTypes by remember(allTypes) {
                mutableStateOf(if (allTypes.isNotEmpty()) allTypes else emptyList())
            }

            val onTypeToggle: (String) -> Unit = { type ->
                selectedTypes = if (type in selectedTypes) {
                    selectedTypes - type
                } else {
                    selectedTypes + type
                }
            }

            //Radius
            val radiusValues = listOf("20","50","100","200","∞")
            var selectedRadius by remember { mutableStateOf("200") }
            val radiusValue = when (selectedRadius) {
                "20" -> 20
                "50" -> 50
                "100" -> 100
                "200" -> 200
                "∞" -> Int.MAX_VALUE
                else -> 200
            }

            // Filter POIs (radius and types)
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

            // Filter POIs (onlyOneType)
            val onSelectSingleType: (String) -> Unit = { type ->
                selectedTypes = listOf(type)
            }

            // FinalPoiList
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

                // onlyVisited
                baseList = when {
                    showOnlyVisited -> baseList.filter { poi -> visitedPoiIds.contains(poi.id) }
                    !showVisited -> baseList.filterNot { poi -> visitedPoiIds.contains(poi.id) }
                    else -> baseList
                }

                // Favorite POIs
                if (showOnlyFavorites) {
                    baseList = baseList.filter { poi -> favoriteIds.contains(poi.id) }
                }

                // Sort (km)
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

            // OnDismis()
            fun resetFiltersUndScreens() {
                selectedTypes = allTypes
                selectedRadius = "200"
                showOnlyVisited = false
                showOnlyFavorites = false

                showListPOIScreen = false
                showStatisticsScreen = false
                showMapScreen = false
                showProfileScreen = false
                showPOIStoreScreen = false

                selectedItem = "main"
                mainStateViewModel.refreshRegions()
            }

            // --- NAVIGATION ---

            //TopAppBar
            @Composable
            fun showTopAppBar ()
            {
                TopAppBar(
                    currentGP = currentGP,
                    onItemSelected = { selectedItem = it },
                    topBarTitle =
                        if (showListPOIScreen) {
                            if (showOnlyFavorites)"favorites"
                            else  "${finalPOIList.size} мест рядом с $currentCity"
                        }
                        else if (showStatisticsScreen) "statistic"
                        else if (showProfileScreen) "profile"
                        else "main",
                    onDismiss = {resetFiltersUndScreens()},
                    currentLanguage = currentLanguage,
                ) }

            //NavigationBar
            @Composable
            fun showNavigationBar() {
                NavigationBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
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
                    onDismiss = {resetFiltersUndScreens()},
                    currentLanguage = currentLanguage,
                )}

            //LocationPanel
            @Composable
            fun showLocationPanel() {
                LocationPanel(
                    onShowScreenType =
                        if (showMapScreen) "map"
                        else "main",
                    onLocationSelected = { city, latLng ->
                        val (lat, lng) = latLng
                        locationViewModel.setManualLocation(city, lat, lng)
                    },
                    onRequestGPS = { onRequestLocationChange() },
                    userCurrentCity = currentCity,
                    onDismiss = {resetFiltersUndScreens()},
                    currentLanguage = currentLanguage,
                )}

            //FiltersButtons
            @Composable
            fun showFiltersButtons() {
                FiltersButtons(
                    onShowScreenType =
                        if (showMapScreen) "map"
                        else "list",
                    userCurrentCity = currentCity,
                    onRequestGPS = { onRequestLocationChange() },
                    selectedRadius = selectedRadius,
                    onRadiusChange = { selectedRadius = it },
                    onOpenMapScreen = { showMapScreen = true },
                    onOpenListScreen = {showListPOIScreen = true},
                    onOpenFilters = { showFiltersPanel = true },
                    onDismiss = { showMapScreen = false },
                    radiusValues = radiusValues,
                    currentUnits = currentUnits,
                    currentLanguage = currentLanguage,
                )
            }

            //StoreBanner
            @Composable
            fun showStoreBanner () {

                val visitedPois = poiList.filter { poi -> visitedPoiIds.contains(poi.id) }
                val exploredPercentage = if (poiList.isNotEmpty()) {
                    (visitedPois.size.toDouble() / poiList.size * 100).roundToInt()
                } else {
                    0
                }

                StoreBanner(
                    totalPOIs = poiList.size,
                    exploredPercentage = exploredPercentage,
                    currentLanguage = currentLanguage,
                    onOpenStore = {
                        resetFiltersUndScreens()
                        showPOIStoreScreen = true}
                )
            }

            //MapScreen
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
            //ListPOIScreen
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
                    allReviews = reviewsList,
                    currentUnits = currentUnits,
                    currentLanguage = currentLanguage,
                )
            }
            //MainContent
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
                    showStoreBanner = {showStoreBanner () },
                    allReviews = reviewsList,
                    currentUnits = currentUnits,
                    currentLanguage = currentLanguage,
                )
            }

            // StoreScreen
            if (showPOIStoreScreen) {
                StoreScreen(
                    isInitialSelection = false,
                    translateViewModel = translateViewModel,
                    pointsViewModel = pointsViewModel,
                    onRegionChosen = {
                        {resetFiltersUndScreens()}
                    },
                    onDismiss = {resetFiltersUndScreens()},
                    userPreferences = userPreferences,
                    dataRepository = dataRepository,
                    userRemoteDataSource = userRemoteDataSource,
                )
            }

            // StatisticsScreen
            if (showStatisticsScreen) {
                StatisticsScreen(
                    userPOIList = finalPOIList,
                    totalPOIList = poiList,
                    allTypes = allTypes,
                    showNavigationBar = { showNavigationBar() },
                    showTopAppBar = { showTopAppBar () },
                    pointsViewModel = pointsViewModel,
                    translateViewModel = translateViewModel,
                    statisticsViewModel = statisticsViewModel,
                    leaderboardViewModel = leaderboardViewModel,
                    typeIcons = typeIcons
                )
            }

            // ProfileScreen
            if(showProfileScreen) {
                ProfileScreen(
                    showNavigationBar = { showNavigationBar() },
                    showTopAppBar = { showTopAppBar () },
                    showStoreBanner = {showStoreBanner () },
                    themeViewModel = themeViewModel,
                    loginViewModel = loginViewModel,
                    translateViewModel = translateViewModel,
                    profileViewModel = profileViewModel,
                    onLoggedOut = onLoggedOut,
                    isPremium = isPremium
                )
            }

            // FiltersPanel
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
                        radiusValues = radiusValues,
                        currentUnits = currentUnits,
                        currentLanguage = currentLanguage,
                        typeIcons = typeIcons
                    )
                }
            }

            // POIFullScreen (in MapScreen)
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
                        locationViewModel = locationViewModel,
                        loginViewModel = loginViewModel,
                        isPremium = isPremium,
                        currentUnits = currentUnits,
                        currentLanguage = currentLanguage,
                    )
                }
            }

            // POIFullScreen
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
                    locationViewModel = locationViewModel,
                    loginViewModel = loginViewModel,
                    isPremium = isPremium,
                    currentUnits = currentUnits,
                    currentLanguage = currentLanguage,
                )
            }
        }

    } ?: LoadingOverlay(title = LocalizerUI.t("loading", currentLanguage))
}