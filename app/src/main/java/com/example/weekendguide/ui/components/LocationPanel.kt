package com.example.weekendguide.ui.components

import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.locales.LocalizerUI
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

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
    currentLanguage: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val imm = context.getSystemService(InputMethodManager::class.java)
    val view = LocalView.current
    val sound = LocalView.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val placesClient = remember { Places.createClient(context) }

    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf<List<SuggestionItem>>(emptyList()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val defaultLabel = userCurrentCity ?: LocalizerUI.t("city_or_address", currentLanguage)
    val defaultSuggestionText = LocalizerUI.t("near_me", currentLanguage)

    LaunchedEffect(query.text, isFocused) {
        val baseSuggestions = listOf(SuggestionItem.CurrentLocation)
        if (isFocused) {
            if (query.text.isNotBlank()) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query.text)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        suggestions = baseSuggestions + response.autocompletePredictions.map {
                            SuggestionItem.Prediction(it)
                        }
                    }
                    .addOnFailureListener {
                        suggestions = baseSuggestions
                    }
            } else {
                suggestions = baseSuggestions
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
            label = { Text(defaultLabel) }, // если defaultLabel — строка, стоит заменить
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            leadingIcon = {
                if (onShowScreenType == "map") {
                    IconButton(onClick = {
                        sound.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        onDismiss()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = LocalizerUI.t("back", currentLanguage)
                        )
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
                        Icon(
                            Icons.Default.Close,
                            contentDescription = LocalizerUI.t("clear", currentLanguage)
                        )
                    }
                }
            },
            singleLine = true,
            interactionSource = interactionSource,
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
                    .background(Color.White.copy(alpha = 0.9f))
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
                                        query = TextFieldValue(defaultSuggestionText) // можно заменить на локализованное
                                        suggestions = emptyList()
                                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                                        focusManager.clearFocus()
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
                                    text = LocalizerUI.t("current_location", currentLanguage),
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
                                                    onLocationSelected(
                                                        name,
                                                        Pair(latLng.latitude, latLng.longitude)
                                                    )
                                                }
                                                suggestions = emptyList()
                                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                                                focusManager.clearFocus()
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
