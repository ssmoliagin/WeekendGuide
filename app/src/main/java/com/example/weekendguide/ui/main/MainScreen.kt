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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
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
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context = LocalContext.current) {
    //состояние окон
    var showMap by remember { mutableStateOf(false) }
    var showFiltersPanel by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showPOIInMap by remember { mutableStateOf(false) }
    var showListPoi by remember { mutableStateOf(false) }
    var onSortPOI by remember { mutableStateOf(false) }
    var showFullPOI by remember { mutableStateOf(false) }
    var showPOICardTypeMini by remember { mutableStateOf(false) }
    var showPOICardTypeList by remember { mutableStateOf(false) }




    var showOnlyFavorites by remember { mutableStateOf(false) } //скрытый фильтр ТОЛЬКО избранные
    var showOnlyVisited by remember { mutableStateOf(false) } // скрытый фильтр ТОЛЬКО посещенные
    var showVisited by remember { mutableStateOf(true) } // показать посещенные

    var selectedPOI by remember { mutableStateOf<POI?>(null) }

    val locationViewModel: LocationViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val prefs = UserPreferences(context)

    val region by produceState<Region?>(initialValue = null) {
        value = prefs.getHomeRegion()
    }

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
        val viewModel: POIViewModel = viewModel(factory = POIViewModelFactory(context, reg))
        val poiList by viewModel.poiList.collectAsState()

        val favoriteIds by viewModel.favoriteIds.collectAsState() // НОВОЕ ИЗБРАННЫЕ ПОИ
        val onFavoriteClick: (String) -> Unit = { poiId ->
            viewModel.toggleFavorite(poiId)
        }

        //посещенные пои
        val visitedPoiIds by viewModel.visitedPoiIds.collectAsState()
        val onCheckpointClick: (String) -> Unit = { poiId ->
            viewModel.markPoiVisited(poiId)
        }


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

        // --- НАВИГАЦИЯ ---

        //основной экран или карта
        if (showMap) {
            MapScreen(
                userPOIList = finalPOIList,
                userLocation = userLocation,
                userCurrentCity = currentCity,
                selectedRadius = selectedRadius,
                onDismiss = { showMap = false },
                onOpenLocation = { showLocationDialog = true },
                onOpenFilters = { showFiltersPanel = true },
                onSelectPOI = { poi -> selectedPOI = poi },
                onOpenPOIinMap = {showPOIInMap = true},
                onOpenListScreen = {showListPoi = true},
                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                onFavoriteClick = onFavoriteClick,
                onRequestGPS = onRequestLocationChange,
                onLocationSelected = { city, latLng ->
                    // Распаковываем lat и lng
                    val (lat, lng) = latLng
                    locationViewModel.setManualLocation(city, lat, lng)
                },
                showLocationDialog = {
                    LocationDialog(
                        onShowScreenType = "map",
                        onLocationSelected = { city, latLng ->
                            // Распаковываем lat и lng
                            val (lat, lng) = latLng
                            locationViewModel.setManualLocation(city, lat, lng)
                        },
                        onRequestGPS = onRequestLocationChange,
                        userCurrentCity = currentCity,
                        onDismiss = { showMap = false },
                    )
                },
                showFiltersButtons = {
                    FiltersButtons(
                        onShowScreenType = "map",
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
            )
        } else if (showListPoi) {
            ListPOIScreen(
                userPOIList = finalPOIList,
                userLocation = userLocation,
                userCurrentCity = currentCity,
                selectedRadius = selectedRadius,
                onDismiss = {
                    showListPoi = false
                    selectedTypes = allTypes
                    showOnlyFavorites = false // сбрасываем фильтр
                    showOnlyVisited = false
                            },
                onOpenLocation = { showLocationDialog = true },
                onOpenFilters = { showFiltersPanel = true },
                onSelectPOI = { poi -> selectedPOI = poi },
                onOpenProfile = { showProfile = true },
                onOpenMapScreen = {showMap = true},
                onSortPOIButton = {onSortPOI = true},
                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                onFavoriteClick = onFavoriteClick,
                onShowPOICardTypeList = {showPOICardTypeList},
                onPOIClick = {showFullPOI = true},
                showTopAppBar = { TopAppBar(
                    topBarTitle = "${finalPOIList.size} мест рядом с $currentCity",
                    onDismiss = {
                        selectedTypes = allTypes
                        showOnlyFavorites = false
                        showOnlyVisited = false
                        showListPoi = false
                    },
                ) },
                showFiltersButtons = {
                    FiltersButtons(
                        onShowScreenType = "list",
                        userCurrentCity = currentCity,
                        onRequestGPS = onRequestLocationChange,
                        selectedRadius = selectedRadius,
                        onRadiusChange = { selectedRadius = it },
                        onOpenMapScreen = { showMap = true },
                        onOpenListScreen = {showListPoi = true},
                        onOpenFilters = { showFiltersPanel = true },
                        onDismiss = { showMap = false },
                    )
                },
            )
        }

        else {
            MainContent(
                userPOIList = filteredPOIList,
                userLocation = userLocation,
                userCurrentCity = currentCity,
                onOpenMapScreen = { showMap = true },
                onOpenListScreen = {showListPoi = true},
                onOpenLocation = { showLocationDialog = true },
                onOpenFilters = { showFiltersPanel = true },
                onOpenProfile = { showProfile = true },
                onSelectSingleType = onSelectSingleType,
                onShowFavoritesList = {
                    showOnlyFavorites = true
                    showListPoi = true
                },
                onShowVisitedList = {
                    showOnlyVisited = true
                    showListPoi = true
                },

                isFavorite = { poi -> favoriteIds.contains(poi.id) },
                isVisited = { poi -> visitedPoiIds.contains(poi.id) },
                onFavoriteClick = onFavoriteClick,
                onShowPOICardTypeMini = {showPOICardTypeMini},
                onShowPOICardTypeList = {showPOICardTypeList},
                onPOIClick = {showFullPOI = true},
                onSelectPOI = { poi -> selectedPOI = poi },
                onLocationSelected = { city, latLng ->
                    // Распаковываем lat и lng
                    val (lat, lng) = latLng
                    locationViewModel.setManualLocation(city, lat, lng)
                },
                onRequestGPS = onRequestLocationChange,
                selectedRadius = selectedRadius,
                onRadiusChange = { selectedRadius = it },
                showNavigationBar = {
                    NavigationBar(
                        onShowFavoritesList = {
                        showOnlyFavorites = true
                        showListPoi = true
                    },
                        onOpenProfile = { showProfile = true },

                )},
                showTopAppBar = { TopAppBar(
                    topBarTitle = "main",
                    onDismiss = {
                        selectedTypes = allTypes
                        showOnlyFavorites = false
                        showOnlyVisited = false
                        showListPoi = false
                    },
                ) },
                showLocationDialog = {
                    LocationDialog(
                        onShowScreenType = "main",
                        onLocationSelected = { city, latLng ->
                            // Распаковываем lat и lng
                            val (lat, lng) = latLng
                            locationViewModel.setManualLocation(city, lat, lng)
                        },
                        onRequestGPS = onRequestLocationChange,
                        userCurrentCity = currentCity,
                        onDismiss = { showMap = false },
                    )
                },
                showFiltersButtons = {
                    FiltersButtons(
                        onShowScreenType = "main",
                        userCurrentCity = currentCity,
                        onRequestGPS = onRequestLocationChange,
                        selectedRadius = selectedRadius,
                        onRadiusChange = { selectedRadius = it },
                        onOpenMapScreen = { showMap = true },
                        onOpenListScreen = {showListPoi = true},
                        onOpenFilters = { showFiltersPanel = true },
                        onDismiss = { showMap = false },
                    )
                },
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
                    showOnlyFavorites = showOnlyFavorites,
                    onToggleShowFavorites = { showOnlyFavorites = !showOnlyFavorites },
                    showVisited = showVisited,
                    onToggleShowVisited = { showVisited = !showVisited },
                    showOnlyVisited = showOnlyVisited,
                    onToggleShowOnlyVisited = { showOnlyVisited = !showOnlyVisited },
                    onDismiss = { showFiltersPanel = false }
                )
            }
        }

        /*
        // ✅ Панель Выбор локации
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

         */

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
                    onFavoriteClick = { viewModel.toggleFavorite(poi.id) },
                    isVisited = visitedPoiIds.contains(poi.id),
                    userLocation = userLocation,
                    userCurrentCity = currentCity,
                    onDismiss = {
                        selectedPOI = null
                        showFullPOI = false
                    },
                    viewModel = viewModel,
                    onRequestGPS = onRequestLocationChange,
                    onCheckpointClick = { viewModel.markPoiVisited(poi.id) },
                )

                /*
                POICard(
                    poi = poi,
                    isFavorite = favoriteIds.contains(poi.id),
                    onFavoriteClick = { viewModel.toggleFavorite(poi.id) },
                    onClick = {showFullPOI = true},
                    userLocation = userLocation,
                    userCurrentCity = currentCity,
                    cardType = "map",
                    onSelectPOI = { poi -> selectedPOI = poi },
                )

                 */
            }
        }

        //✅ POI на весь экран
        if (showFullPOI && poi != null) {
            POIFullScreen (
                poi = poi,
                isFavorite = favoriteIds.contains(poi.id),
                onFavoriteClick = { viewModel.toggleFavorite(poi.id) },
                isVisited = visitedPoiIds.contains(poi.id),
                userLocation = userLocation,
                userCurrentCity = currentCity,
                onDismiss = {
                    selectedPOI = null
                    showFullPOI = false
                },
                viewModel = viewModel,
                onRequestGPS = onRequestLocationChange,
                onCheckpointClick = { viewModel.markPoiVisited(poi.id) },
            )
        }
        
    } ?: LoadingScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(
    onShowFavoritesList: () -> Unit,
    onOpenProfile: () -> Unit,
)
{
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val sound = LocalView.current


    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            label = { Text("Поиск") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onShowFavoritesList()
            },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Сохранённое") },
            label = { Text("Сохранённое") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                //
                      },
            icon = { Icon(Icons.Default.Star, contentDescription = "Достижения") },
            label = { Text("Достижения") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onOpenProfile()
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label = { Text("Профиль") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar (
    topBarTitle: String,
    onDismiss: () -> Unit
) {

    val title = when (topBarTitle) {
        "main" -> "Weekend Guide"
        "favorites" -> "Избранное"
        else -> topBarTitle.toString()
    }


    TopAppBar(
        title = { Text(title, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("125 GP", color = Color.White)
            }
        },
        navigationIcon = {
            if (topBarTitle != "main") {
                IconButton(onClick = onDismiss) {
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
fun LocationDialog(
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
                    imageVector = Icons.Default.Place,
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
                    imageVector = Icons.Default.LocationOn,
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
                imageVector = Icons.Default.Settings,
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
fun MainContent(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    userCurrentCity: String?,
    onOpenMapScreen: () -> Unit,
    onOpenListScreen: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectSingleType: (String) -> Unit,
    onShowFavoritesList: () -> Unit,
    onShowVisitedList: () -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    onFavoriteClick: (String) -> Unit,
    onShowPOICardTypeMini: () -> Unit,
    onShowPOICardTypeList: () -> Unit,
    onPOIClick: () -> Unit,
    onSelectPOI: (POI) -> Unit,

    onLocationSelected: (String, Pair<Double, Double>) -> Unit,
    onRequestGPS: () -> Unit,
    selectedRadius: String,
    onRadiusChange: (String) -> Unit,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    showLocationDialog: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,

    ) {
    
    val listState = rememberLazyListState()
    var randomPOI by remember { mutableStateOf<POI?>(null) }
    val coroutineScope = rememberCoroutineScope()
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
            showLocationDialog()

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
    userCurrentCity: String?,

    selectedRadius: String,

    onDismiss: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenFilters: () -> Unit,
    onSelectPOI: (POI) -> Unit,
    onOpenPOIinMap: () -> Unit,
    onOpenListScreen: () -> Unit,

    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    onFavoriteClick: (String) -> Unit,
    onRequestGPS: () -> Unit,
    onLocationSelected: (String, Pair<Double, Double>) -> Unit,
    showLocationDialog: @Composable () -> Unit,
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
/*
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

 */






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
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .fillMaxSize()
            ) {
                //Поле выбор локации
                showLocationDialog()

                Spacer(modifier = Modifier.height(4.dp))

                // кнопки фильтры
                showFiltersButtons()
            }



            // 🎯 ПАНЕЛЬ — единая "таблетка" с тремя элементами


            /*
            Row(
                modifier = Modifier
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔙 Кнопка "Назад"
                IconButton(onClick = onDismiss) {
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
                        .clickable { onOpenLocation() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = userCurrentCity ?: "Определение местоположения...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (userCurrentCity != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
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




// 🔘 Кнопки "Списком"
            Row(
                modifier = Modifier
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {
                        onOpenListScreen()
                        onDismiss()
                    },
                    label = { Text("Списком", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Default.List, contentDescription = null)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White,
                        labelColor = Color.Black,
                        leadingIconContentColor = Color.Black
                    )
                )

            }

             */




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
    showOnlyFavorites: Boolean,
    onToggleShowFavorites: () -> Unit,
    showVisited: Boolean,
    onToggleShowVisited: () -> Unit,
    showOnlyVisited: Boolean,
    onToggleShowOnlyVisited: () -> Unit,
    onDismiss: () -> Unit
) {
    val radiusValues = listOf("20", "50", "100", "200", "∞")
    val radiusSliderPosition = radiusValues.indexOfFirst {
        it.removeSuffix("км") == selectedRadius.removeSuffix("км")
    }.coerceAtLeast(0)

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

            // ⭐ Фильтр по избранным

            /*
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onToggleShowFavorites() }
            ) {
                Checkbox(
                    checked = showOnlyFavorites,
                    onCheckedChange = { onToggleShowFavorites() }
                )
                Text("Показывать только избранные", style = MaterialTheme.typography.bodyLarge)
            }

            // ✅ Посещённые

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onToggleShowOnlyVisited() }
            ) {
                Checkbox(
                    checked = showOnlyVisited,
                    onCheckedChange = { onToggleShowOnlyVisited() }
                )
                Text("Показывать только посещённые", style = MaterialTheme.typography.bodyLarge)
            }

             */

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

            // ✅ Типы мест
            Text("Типы мест", style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedButton(onClick = onSelectAllTypes) {
                    Text("Выбрать все")
                }
                OutlinedButton(onClick = onClearAllTypes) {
                    Text("Убрать все")
                }
            }

            FlowRow(
          //      mainAxisSpacing = 8.dp,
           //     crossAxisSpacing = 8.dp,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                allTypes.forEach { type ->
                    FilterChip(
                        selected = selectedTypes.contains(type),
                        onClick = { onTypeToggle(type) },
                        label = { Text(type) }
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPOIScreen(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    userCurrentCity: String?,
    selectedRadius: String,
    onDismiss: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenFilters: () -> Unit,
    onSelectPOI: (POI) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMapScreen: () -> Unit,
    onSortPOIButton: () -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,

    onFavoriteClick: (String) -> Unit,
    onShowPOICardTypeList: () -> Unit,
    onPOIClick: () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,
) {
    val listState = rememberLazyListState()
    val poiCount = userPOIList.size

    Scaffold(

        //ШАПКА
        topBar = {
            showTopAppBar()
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // кнопки фильтры
            showFiltersButtons()

            Spacer(modifier = Modifier.height(4.dp))

            /*
            // 🏷 Заголовок
            Text(
                text = "$poiCount мест рядом с ${userCurrentCity ?: "вами"}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )



            // 🔘 Кнопки сортировки, фильтра и карты
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val buttonModifier = Modifier.weight(1f)

                //Кнопка Сортировка
                AssistChip(
                    onClick = {
                        onSortPOIButton()
                    },
                    label = {
                        Text("Сортировать")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )

                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text("Фильтры", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    modifier = buttonModifier
                )

                AssistChip(
                    onClick = onOpenMapScreen,
                    label = { Text("Карта", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = buttonModifier
                )
            }
             */

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

@Composable
fun POICard(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,

    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    cardType: String? = null, // "map", "list", "mini"
    onClick: () -> Unit,
    onSelectPOI: (POI) -> Unit,

    ) {
    val cardModifier: Modifier
    val imageModifier: Modifier
    val isImageLeft: Boolean

    when (cardType) {
        "map" -> {
            cardModifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable {
                    onSelectPOI(poi)
                    onClick()
                }
            imageModifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
            isImageLeft = true
        }
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
        if (isImageLeft) {
            /* ненжно больше на карте
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = imageModifier
                ) {
                    poi.imageUrl?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.White.copy(alpha = 0.6f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isVisited) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = "Избранное",
                            tint = if (isVisited) Color.Green else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.White.copy(alpha = 0.6f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .weight(1f)
                ) {
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

             */
        } else {
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
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
    viewModel: POIViewModel,
    onRequestGPS: () -> Unit,
    onCheckpointClick: () -> Unit,
) {




    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }


    val wikiDescription by viewModel.wikiDescription.collectAsState()

    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val location by locationViewModel.location.collectAsState()


    LaunchedEffect(poi.title) {
        viewModel.loadWikipediaDescription(poi.title)
    }

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

                if (distanceMeters < 1000) {
                    viewModel.markPoiVisited(poi.id)
                } else {
                    Toast.makeText(context, "Вы слишком далеко от точки", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка при определении GPS", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

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
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
            Button(
                onClick = { handleCheckpointClick() },
                enabled = !isVisited,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVisited) Color.Gray else Color.Green//MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isVisited) "Уже посещено" else "Чекпоинт",
                    color = Color.White
                )
            }
        }
    }
}



