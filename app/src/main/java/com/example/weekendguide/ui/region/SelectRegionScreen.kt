package com.example.weekendguide.ui.region

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weekendguide.data.model.Region
import com.example.weekendguide.viewmodel.RegionViewModel
import com.example.weekendguide.data.preferences.UserPreferences
import com.example.weekendguide.viewmodel.TranslateViewModel
import kotlinx.coroutines.launch

@Composable
fun SelectRegionScreen(
    onRegionSelected: () -> Unit,
    translateViewModel: TranslateViewModel,
    regionViewModel: RegionViewModel = viewModel()

) {
    val countries by regionViewModel.countries.collectAsState()
    val regionsByCountry by regionViewModel.regionsByCountry.collectAsState()
    val loading by regionViewModel.loading.collectAsState()
    val error by regionViewModel.error.collectAsState()

    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

    val coroutineScope = rememberCoroutineScope()


    val language by translateViewModel.language.collectAsState()


    var selectedCountryCode by remember { mutableStateOf<String?>(null) }
    var selectedRegion by remember { mutableStateOf<Region?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            error != null -> {
                Text(
                    text = error ?: "Произошла ошибка",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedCountryCode == null) {
                        // Выбор страны
                        items(countries) { country ->
                            val countryName = when (language) {
                                "ru" -> country.name_ru
                                "de" -> country.name_de
                                else -> country.name_en
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCountryCode = country.countryCode // ← выбираем страну
                                    }
                            ) {
                                Text(
                                    text = countryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        // Выбор региона внутри страны
                        val regions = regionsByCountry[selectedCountryCode] ?: emptyList()

                        item {
                            TextButton(onClick = { selectedCountryCode = null }) {
                                Text("← Назад к выбору страны")
                            }
                        }

                        if (regions.isEmpty()) {
                            item {
                                Text(
                                    text = "Нет доступных регионов.",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            items(regions) { region ->
                                val name = region.name[language] ?: region.name["en"] ?: "Без названия"
                                val description = region.description?.get(language) ?: ""

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRegion = region
                                            showConfirmDialog = true
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = name, style = MaterialTheme.typography.titleMedium)
                                        if (description.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = description, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Диалог подтверждения выбора региона
        if (showConfirmDialog && selectedRegion != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Подтверждение") },
                text = {
                    val regionName = selectedRegion?.name?.get(language) ?: "этот регион"
                    Text("Вы действительно хотите выбрать $regionName как основной регион?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            coroutineScope.launch {
                                selectedRegion?.let { region ->
                                    regionViewModel.downloadAndCacheRegionPOI(region)
                                    userPreferences.addPurchasedRegion(region.region_code)
                                    userPreferences.addPurchasedCountries(region.country_code)
                                    userPreferences.saveHomeRegion(region)
                                    onRegionSelected()
                                }
                            }
                        }
                    ) {
                        Text("Да")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
