package com.example.weekendguide.ui.poi

import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    val (cardModifier, imageModifier) = when (cardType) {
        "mini" -> Modifier
            .width(200.dp)
            .height(310.dp) to Modifier
            .fillMaxWidth()
            .height(200.dp)
        else -> Modifier
            .fillMaxWidth()
            .height(400.dp) to Modifier
            .fillMaxWidth()
            .height(300.dp)
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
        modifier = cardModifier.clickable {
            onSelectPOI(poi)
            onClick()
        },
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
                        contentDescription = "Visited",
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
                        contentDescription = "Favorites",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                // ⭐️ Rating row
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

                    Text(
                        text = String.format(" %.1f (%d)", if (reviewsCount > 0) averageRating else 5.0, reviewsCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reviewsCount > 0) LocalContentColor.current else Color.Gray
                    )
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
                        text = "$it km from $userCurrentCity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
