package com.example.weekendguide.ui.poi

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

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
    pointsViewModel: PointsViewModel,
    locationViewModel: LocationViewModel
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
    //val locationViewModel: LocationViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(poi.title) {
        poiViewModel.loadWikipediaDescription(poi.title)
        Log.d("POIViewModel", "Got wiki: ${poi.title}")
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
                        pointsViewModel.checkAndAwardGPForPOI(poi, locationViewModel) { success ->
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