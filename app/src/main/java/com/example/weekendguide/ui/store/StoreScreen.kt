package com.example.weekendguide.ui.store

import android.view.SoundEffectConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.StoreViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.model.Country
import kotlinx.coroutines.launch
fun countryCodeToFlagEmoji(code: String): String {
    return if (code.length == 2) {
        code.uppercase().map {
            Character.toChars(0x1F1E6 + (it.code - 'A'.code)).concatToString()
        }.joinToString("")
    } else {
        "🏳️"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoreScreen(
    isInitialSelection: Boolean,
    onRegionChosen: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    translateViewModel: TranslateViewModel,
    pointsViewModel: PointsViewModel,
    onDismiss: () -> Unit? = {},
) {
    val countries by storeViewModel.countries.collectAsState()
    val regionsByCountry by storeViewModel.regionsByCountry.collectAsState()
    val purchasedRegions by storeViewModel.purchasedRegions.collectAsState()
    val language by translateViewModel.language.collectAsState()
    val currentGP by pointsViewModel.currentGP.collectAsState()

    var selectedCountryCode by remember { mutableStateOf<String?>(null) }
    var selectedRegion by remember { mutableStateOf<Region?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    val coroutineScope = rememberCoroutineScope()
    val sound = LocalView.current
    val COST = 1

    val isLoading by storeViewModel.loading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Загружаем страны и регионы")
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        TextButton(onClick = { pointsViewModel.addGP(1000) }) {
                            Text("+1000GP", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$currentGP 🏆", color = Color.White)
                        }
                    },
                    navigationIcon = {
                        if (!isInitialSelection) {
                            IconButton(onClick = {
                                sound.playSoundEffect(SoundEffectConstants.CLICK)
                                onDismiss()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {

                if (selectedCountryCode == null) {

                    // Список с якорями
                    Box(modifier = Modifier.fillMaxSize()) {
                        val localizedCountries = countries.map { country ->
                            val name = when (language) {
                                "ru" -> country.name_ru
                                "de" -> country.name_de
                                else -> country.name_en
                            }
                            name to country
                        }
                            .filter { (name, _) -> name.contains(searchQuery, ignoreCase = true) }
                            .sortedBy { it.first.lowercase() }

                        val grouped = localizedCountries.groupBy { it.first.first().uppercaseChar() }

                        val headers = grouped.keys.sorted()
                        val indexedList = mutableListOf<Pair<String?, Pair<String, Country>?>>()

                        grouped.forEach { (letter, items) ->
                            indexedList.add(letter.toString() to null)
                            items.forEach {
                                indexedList.add(null to it)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Поиск страны") },
                                        singleLine = true
                                    )
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                                        }
                                    }
                                }
                            }

                            itemsIndexed(indexedList) { index, item ->
                                val (header, countryData) = item
                                if (header != null) {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .animateItemPlacement()
                                    )
                                } else if (countryData != null) {
                                    val (name, country) = countryData
                                    val flag = countryCodeToFlagEmoji(country.countryCode)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCountryCode = country.countryCode
                                            }
                                            .animateItemPlacement()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(flag, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                                            Text(name, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        }

                        if (searchQuery.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 4.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                headers.forEach { letter ->
                                    Text(
                                        text = letter.toString(),
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clickable {
                                                val index = indexedList.indexOfFirst { it.first == letter.toString() }
                                                if (index != -1) {
                                                    scope.launch {
                                                        listState.animateScrollToItem(index)
                                                    }
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }

                } else {
                    val regions = regionsByCountry[selectedCountryCode] ?: emptyList()

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            TextButton(onClick = { selectedCountryCode = null }) {
                                Text("← Назад к странам")
                            }
                        }

                        if (regions.isEmpty()) {
                            item {
                                Text(
                                    text = when (language) {
                                        "ru" -> "Нет доступных регионов для этой страны"
                                        "de" -> "Keine verfügbaren Regionen für dieses Land"
                                        else -> "No available regions for this country"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        } else {
                            items(regions) { region ->
                                val name = region.name[language] ?: region.name["en"] ?: "Без названия"
                                val isPurchased = purchasedRegions.contains(region.region_code)

                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(name, style = MaterialTheme.typography.titleMedium)

                                        if (isPurchased) {
                                            Text("✅ Уже куплен", color = Color.Green)
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    selectedRegion = region
                                                    showDialog = true
                                                },
                                                enabled = isInitialSelection || currentGP >= COST
                                            ) {
                                                Text(if (isInitialSelection) "Выбрать" else "Купить за $COST GP")
                                            }
                                            if (!isInitialSelection && currentGP < COST) {
                                                Text("Недостаточно GP", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showDialog && selectedRegion != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Text(if (isInitialSelection) "Выбор региона" else "Подтвердите покупку")
                        },
                        text = {
                            val regionName = selectedRegion?.name?.get(language) ?: "этот регион"
                            Text(
                                if (isInitialSelection)
                                    "Вы хотите выбрать $regionName как основной регион?"
                                else
                                    "Вы хотите купить $regionName за $COST GP?"
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                coroutineScope.launch {
                                    selectedRegion?.let { region ->
                                        if (!isInitialSelection && currentGP < COST) return@launch

                                        if (!isInitialSelection) {
                                            pointsViewModel.spentGP(COST)
                                        }

                                        storeViewModel.purchaseRegionAndLoadPOI(region, translateViewModel)
                                        userPreferences.addRegionInCollection(region)
                                        onRegionChosen()
                                    }
                                }
                            }) {
                                Text(if (isInitialSelection) "Выбрать" else "Купить")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }
        }
    }
}
