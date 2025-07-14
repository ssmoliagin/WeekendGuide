package com.example.weekendguide.ui.components

import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

// Объекты для элементов списка — чтобы добавить "Рядом со мной" и обычные предсказания
sealed class SuggestionItem {
    object CurrentLocation : SuggestionItem()
    data class Prediction(val prediction: AutocompletePrediction) : SuggestionItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPanel(
    userCurrentCity: String?,
    onLocationSelected: (String, Pair<Double, Double>) -> Unit,
    onRequestGPS: () -> Unit,
    onShowScreenType: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val imm = context.getSystemService(InputMethodManager::class.java)
    val sound = LocalView.current
    val placesClient = remember { Places.createClient(context) }

    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf<List<SuggestionItem>>(emptyList()) }
    var isFocused by remember { mutableStateOf(false) }

    val defaultLabel = userCurrentCity ?: "Город или адрес"
    val defaultSuggestionText = "Рядом со мной"

    // Запрос подсказок при изменении текста
    LaunchedEffect(query.text) {
        if (query.text.isNotBlank()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query.text)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    // Добавляем в начало элемент "Рядом со мной"
                    suggestions = listOf(SuggestionItem.CurrentLocation) +
                            response.autocompletePredictions.map { SuggestionItem.Prediction(it) }
                }
                .addOnFailureListener {
                    // В случае ошибки показываем только "Рядом со мной"
                    suggestions = listOf(SuggestionItem.CurrentLocation)
                    it.printStackTrace()
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
            onValueChange = {
                query = it
                isFocused = true
            },
            label = { Text(defaultLabel) },
            modifier = Modifier
                .fillMaxWidth(),
            leadingIcon = {
                if (onShowScreenType == "map") {
                    IconButton(onClick = {
                        sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            trailingIcon = {
                if (query.text.isNotEmpty()) {
                    IconButton(onClick = {
                        query = TextFieldValue("")
                        suggestions = emptyList()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (isFocused && suggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                items(suggestions) { item ->
                    when (item) {
                        is SuggestionItem.CurrentLocation -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                        onRequestGPS()
                                        query = TextFieldValue(defaultSuggestionText)
                                        suggestions = emptyList()
                                        isFocused = false
                                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = defaultSuggestionText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is SuggestionItem.Prediction -> {
                            val prediction = item.prediction
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                                    val name = place.name ?: ""
                                                    query = TextFieldValue(name)
                                                    onLocationSelected(name, Pair(latLng.latitude, latLng.longitude))
                                                }
                                                suggestions = emptyList()
                                                isFocused = false
                                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                                            }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = prediction.getFullText(null).toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
