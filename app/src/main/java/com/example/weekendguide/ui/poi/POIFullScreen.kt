package com.example.weekendguide.ui.poi

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.ui.components.LoadingOverlay
import com.example.weekendguide.viewmodel.PointsViewModel
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
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
    locationViewModel: LocationViewModel,
    loginViewModel: LoginViewModel,
) {
    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }

    val context = LocalContext.current
    val wikiDescription by poiViewModel.wikiDescription.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    //Персональные данные
    val userInfo by loginViewModel.userData.collectAsState()
    val email = userInfo.email ?: ""
    val displayName = userInfo.displayName ?: ""
    val photoUrl = userInfo.photoUrl
    val name = displayName.ifBlank { email.substringBefore("@") }

    LaunchedEffect(poi.title) {
        poiViewModel.loadWikipediaDescription(poi.title)
        poiViewModel.loadReviews(poi.id)
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

                // *** Новый блок: Средний рейтинг и количество отзывов под фото ***
                item {
                    val allReviews by poiViewModel.reviews.collectAsState()
                    val reviews = allReviews[poi.id] ?: emptyList()
                    val averageRating = if (reviews.isNotEmpty())
                        reviews.map { it.rating }.average()
                    else 0.0
                    val reviewsCount = reviews.size

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Показ звезд среднего рейтинга (округление вниз)
                        val filledStars = averageRating.toInt()
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < filledStars) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("Рейтинг: %.1f (%d отзывов)", averageRating, reviewsCount),
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

                // Карта, как есть (без изменений)...

                item {
                    Text(
                        text = "Отзывы",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    val reviewsMap by poiViewModel.reviews.collectAsState()
                    val poiReviews = reviewsMap[poi.id] ?: emptyList()

                    // *** Добавляем состояние для выбранного рейтинга перед отзывом ***
                    var selectedRating by remember { mutableStateOf(0) }
                    var reviewText by remember { mutableStateOf("") }
                    val context = LocalContext.current

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (poiReviews.isEmpty()) {
                            Text("Нет отзывов. Будьте первым!", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            poiReviews.forEach { review ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!review.userPhotoUrl.isNullOrEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(review.userPhotoUrl),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color.Gray, shape = CircleShape)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color.Gray, shape = CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = review.userName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(review.userName, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                                                    .format(java.util.Date(review.timestamp)),
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Звёзды рейтинга отзыва
                                    Row {
                                        repeat(5) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < review.rating) Color(0xFFFFD700) else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(review.text)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // *** Выбор рейтинга перед отзывом ***
                        Text("Оцените POI:", style = MaterialTheme.typography.bodyMedium)
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Рейтинг $i",
                                    tint = if (i <= selectedRating) Color(0xFFFFD700) else Color.LightGray,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { selectedRating = i }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = {
                                if (it.length <= 500) { // *** Ограничение длины отзыва 500 символов ***
                                    reviewText = it
                                }
                            },
                            placeholder = { Text("Оставьте отзыв (до 500 символов)...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5,
                            singleLine = false,
                            isError = reviewText.length > 500
                        )
                        if (reviewText.length > 500) {
                            Text(
                                "Максимум 500 символов",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null && reviewText.isNotBlank() && selectedRating > 0) {
                                    val alreadyReviewed = poiViewModel.hasUserReviewed(poi.id, currentUser.uid)

                                    if (alreadyReviewed) {
                                        Toast.makeText(context, "Вы уже оставляли отзыв к этой точке", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val review = Review(
                                            poiId = poi.id,
                                            userId = currentUser.uid,
                                            userName = name,
                                            userPhotoUrl = photoUrl,
                                            rating = selectedRating,
                                            text = reviewText,
                                            timestamp = System.currentTimeMillis()

                                        )

                                        poiViewModel.submitReview(
                                            review,
                                            onSuccess = {
                                                reviewText = ""
                                                selectedRating = 0
                                                Toast.makeText(context, "Отзыв отправлен", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = {
                                                Toast.makeText(context, "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.End),
                            enabled = reviewText.isNotBlank() && selectedRating > 0
                        ) {
                            Text("Отправить")
                        }
                    }
                }
            }

            // Кнопка чекпоинта
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


            // Оверлей "Сканирование местности..."
            if (isChecking) {
                LoadingOverlay(title = "Сканирование местности...")
            }
        }
    }
}