package com.example.weekendguide.ui.poi

import android.location.Location
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.viewmodel.POIViewModel
import androidx.compose.runtime.getValue

@Composable
fun POICard(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,
    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    cardType: String? = "list",
    onClick: () -> Unit,
    onSelectPOI: (POI) -> Unit,
    reviews: List<Review> = emptyList(),
    poiViewModel: POIViewModel
) {
    val cardModifier: Modifier
    val imageModifier: Modifier
    val isImageLeft: Boolean

    when (cardType) {
        "mini" -> {
            cardModifier = Modifier
                .width(200.dp)
                .height(310.dp)
                .clickable {
                    onSelectPOI(poi)
                    onClick()
                }
            imageModifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
            isImageLeft = false
        }
        else -> {
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

    val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
    val reviewsCount = reviews.size

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
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

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.White.copy(alpha = 0.6f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Избранные",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {

// ⭐️ Рейтинг — всегда показываем 5 звёзд
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    val filledStars = if (reviewsCount > 0) averageRating.toInt() else 5
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < filledStars) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (reviewsCount > 0) {
                        Text(
                            text = String.format(" %.1f (%d)", averageRating, reviewsCount),
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text(
                            text = "5.0 (0)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = poi.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (cardType != "mini") {
                    Text(
                        text = poi.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

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
