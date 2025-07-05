package com.example.weekendguide.ui.store

import android.view.SoundEffectConstants
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
import com.example.weekendguide.viewmodel.RegionViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.example.weekendguide.data.model.Region
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoiStoreScreen(
    isInitialSelection: Boolean,
    onRegionChosen: () -> Unit,
    regionViewModel: RegionViewModel = viewModel(),
    translateViewModel: TranslateViewModel,
    pointsViewModel: PointsViewModel,
    onDismiss: () -> Unit? = {},
) {
    val countries by regionViewModel.countries.collectAsState()
    val regionsByCountry by regionViewModel.regionsByCountry.collectAsState()
    val purchasedRegions by regionViewModel.purchasedRegions.collectAsState()
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

    Scaffold(
        topBar = {
            TopAppBar(
               // title = { Text("STORE", color = Color.White) },
                title = {
                    TextButton(onClick = { pointsViewModel.addGP(1000) })
                    {
                    Text("+1000GP", color = Color.White)
                    }},
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



            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                if (selectedCountryCode == null) {
                    items(countries) { country ->
                        val name = when (language) {
                            "ru" -> country.name_ru
                            "de" -> country.name_de
                            else -> country.name_en
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountryCode = country.countryCode
                                }
                        ) {
                            Text(text = name, modifier = Modifier.padding(16.dp))
                        }
                    }
                } else {
                    val regions = regionsByCountry[selectedCountryCode] ?: emptyList()

                    item {
                        TextButton(onClick = { selectedCountryCode = null }) {
                            Text("← Назад к странам")
                        }
                    }

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
                                        Text(if (isInitialSelection) "Выбрать" else "Купить за ${COST.toString()} GP")
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
                                "Вы хотите купить $regionName за ${COST.toString()} GP?"
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

                                   //regionViewModel.downloadAndCacheRegionPOI(region, translateViewModel)
                                    regionViewModel.purchaseRegionAndLoadPOI(region, translateViewModel)
                                    //userPreferences.addPurchasedRegion(region.region_code)
                                    //userPreferences.addPurchasedCountries(region.country_code)
                                    userPreferences.saveHomeRegion(region)

                                    if (isInitialSelection) {

                                    }
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
