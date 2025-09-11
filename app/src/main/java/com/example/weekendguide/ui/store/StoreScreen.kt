package com.example.weekendguide.ui.store

import android.view.SoundEffectConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.Country
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.data.repository.DataRepositoryImpl
import com.example.weekendguide.data.repository.UserRemoteDataSource
import com.example.weekendguide.ui.components.LoadingOverlay
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.StoreViewModel
import com.example.weekendguide.viewmodel.StoreViewModelFactory
import com.example.weekendguide.viewmodel.TranslateViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoreScreen(
    isInitialSelection: Boolean,
    onRegionChosen: () -> Unit,
    translateViewModel: TranslateViewModel,
    pointsViewModel: PointsViewModel,
    onDismiss: () -> Unit = {},
    userPreferences: UserPreferences,
    dataRepository: DataRepositoryImpl,
    userRemoteDataSource: UserRemoteDataSource
) {

    val storeViewModel: StoreViewModel = viewModel(
        factory = StoreViewModelFactory(userPreferences, userRemoteDataSource, dataRepository)
    )
    val userData by storeViewModel.userData.collectAsState()
    //val purchasedRegions = userData.purchasedRegions
    val isSubscription = userData.subscription ?: false
    val purchasedRegions: Set<String> = userData.collectionRegions.map { it.region_code }.toSet()

    val countries by storeViewModel.countries.collectAsState()

    val regionsByCountry by storeViewModel.regionsByCountry.collectAsState()
    //val purchasedRegions by storeViewModel.purchasedRegions.collectAsState()
    val currentLanguage by translateViewModel.language.collectAsState()
    val currentGP by pointsViewModel.currentGP.collectAsState()

    var selectedCountryCode by remember { mutableStateOf<String?>(null) }
    var selectedRegion by remember { mutableStateOf<Region?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showInsufficientGPDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sound = LocalView.current
    val COST = 10_000

    val isLoading by storeViewModel.loading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    if (isLoading) LoadingOverlay(title = LocalizerUI.t("loading", currentLanguage))
    else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(LocalizerUI.t("collections_title", currentLanguage), color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    actions = {
                        Row(modifier = Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$currentGP 🏆", color = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            sound.playSoundEffect(SoundEffectConstants.CLICK)
                            if (selectedCountryCode == null) {
                                scope.launch { listState.animateScrollToItem(0) }
                                onDismiss()
                            } else selectedCountryCode = null
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedCountryCode == null) {
                    val localizedCountries = countries.mapNotNull { country ->
                        val name = when (currentLanguage) {
                            "ru" -> country.name_ru
                            "de" -> country.name_de
                            else -> country.name_en
                        }
                        val allRegions = regionsByCountry[country.countryCode]
                        if (allRegions.isNullOrEmpty()) return@mapNotNull null

                        val purchased = allRegions.count { purchasedRegions.contains(it.region_code) }
                        Triple(name, country, "$purchased/${allRegions.size}")
                    }
                        .filter { it.first.contains(searchQuery, ignoreCase = true) }
                        .sortedWith(
                            compareByDescending<Triple<String, Country, String>> {
                                it.third.split("/")[0].toInt() > 0
                            }.thenBy { it.first.lowercase() }
                        )

                    val grouped = localizedCountries.groupBy { it.first.first().uppercaseChar() }
                    val headers = grouped.keys.sorted()
                    val indexedList = mutableListOf<Pair<String?, Triple<String, Country, String>?>>()
                    grouped.forEach { (letter, items) ->
                        indexedList.add(letter.toString() to null)
                        items.forEach { indexedList.add(null to it) }
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = LocalizerUI.t("collections_desc", currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                textAlign = TextAlign.Center
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text(LocalizerUI.t("search_country_placeholder", currentLanguage)) },
                                    singleLine = true
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        }
                        itemsIndexed(indexedList) { _, item ->
                            val (header, countryData) = item
                            if (header != null) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(vertical = 4.dp).animateItem()
                                )
                            } else if (countryData != null) {
                                val (name, country, regionStatus) = countryData
                                val flag = countryCodeToFlagEmoji(country.countryCode)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCountryCode = country.countryCode }
                                        .animateItem()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(flag, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                                        Column {
                                            Text(name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground)
                                            Text("${LocalizerUI.t("regions_unlocked", currentLanguage)} $regionStatus",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (searchQuery.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            headers.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(2.dp).clickable {
                                        val index = indexedList.indexOfFirst { it.first == letter.toString() }
                                        if (index != -1) scope.launch { listState.animateScrollToItem(index) }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    val regions = regionsByCountry[selectedCountryCode] ?: emptyList()

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (regions.isEmpty()) {
                            item {
                                Text(
                                    text = LocalizerUI.t("noregions", currentLanguage),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            items(regions) { region ->
                                val name = region.name[currentLanguage] ?: region.name["en"] ?: "Unnamed"
                                val description = region.description[currentLanguage] ?: ""
                                val isPurchased = purchasedRegions.contains(region.region_code)
                                val flag = countryCodeToFlagEmoji(region.country_code)
                                val bgColor = if (isPurchased) Color(0xFFF0F0F0) else Color.White

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .clickable(enabled = !isPurchased) {
                                            if (isInitialSelection || isSubscription || currentGP >= COST) {
                                                selectedRegion = region
                                                showDialog = true
                                            } else {
                                                selectedRegion = region
                                                showInsufficientGPDialog = true
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(flag, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground)
                                            if (description.isNotEmpty()) {
                                                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isPurchased) Icons.Default.Check else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = if (isPurchased) "Purchased" else "Select",
                                            tint = if (isPurchased) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showDialog && selectedRegion != null) {
                    val regionName = selectedRegion?.name?.get(currentLanguage) ?: "this region"
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Text(if (isInitialSelection || isSubscription) LocalizerUI.t("select_region_title", currentLanguage)
                        else LocalizerUI.t("confirm_purchase_title", currentLanguage),
                            color = MaterialTheme.colorScheme.onBackground) },
                        text = {
                            Text(
                                if (isInitialSelection)
                                    "${regionName}: " + LocalizerUI.t("confirm_select_text", currentLanguage)
                                else if (isSubscription)
                                    "${regionName}: " + LocalizerUI.t("confirm_subscript_text", currentLanguage)
                                else
                                    "${regionName}: " + LocalizerUI.t("confirm_buy_text", currentLanguage) + " $COST GP?"
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                coroutineScope.launch {
                                    selectedRegion?.let { region ->
                                        if (!isInitialSelection || !isSubscription) pointsViewModel.spentGP(COST)
                                        storeViewModel.purchaseRegionAndLoadPOI(region, isSubscription)
                                        onRegionChosen()
                                    }
                                }
                            }) { Text(if (isInitialSelection || isSubscription) LocalizerUI.t("select", currentLanguage) else LocalizerUI.t("buy", currentLanguage)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) { Text(LocalizerUI.t("cancel", currentLanguage)) }
                        }
                    )
                }

                if (showInsufficientGPDialog && selectedRegion != null) {
                    AlertDialog(
                        onDismissRequest = { showInsufficientGPDialog = false },
                        title = { Text(LocalizerUI.t("insufficient_gp_title", currentLanguage),
                            color = MaterialTheme.colorScheme.onBackground) },
                        text = {
                            Text(LocalizerUI.t("insufficient_gp_text", currentLanguage),
                                color = MaterialTheme.colorScheme.onBackground)
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showInsufficientGPDialog = false
                                coroutineScope.launch {
                                    selectedRegion?.let { region ->
                                        storeViewModel.purchaseRegionAndLoadPOI(region, isSubscription)
                                        onRegionChosen()
                                    }
                                }
                            }) { Text(LocalizerUI.t("buy_with_money", currentLanguage)) }
                        },
                    )
                }
            }
        }
    }
}

fun countryCodeToFlagEmoji(code: String): String {
    return if (code.length == 2) {
        code.uppercase().map {
            Character.toChars(0x1F1E6 + (it.code - 'A'.code)).concatToString()
        }.joinToString("")
    } else {
        "🏳️"
    }
}

