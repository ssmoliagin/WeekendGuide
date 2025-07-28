package com.example.weekendguide.ui.poi

import android.location.Location
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.ui.components.LoadingOverlay
import com.example.weekendguide.viewmodel.LocationViewModel
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.POIViewModel
import com.example.weekendguide.viewmodel.PointsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIFullScreen(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,
    isPremium: Boolean,

    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    onDismiss: () -> Unit,
    poiViewModel: POIViewModel,
    pointsViewModel: PointsViewModel,
    locationViewModel: LocationViewModel,
    loginViewModel: LoginViewModel,
    currentLanguage: String,
    currentUnits: String
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

    val userInfo by loginViewModel.userData.collectAsState()
    val email = userInfo.email ?: ""
    val displayName = userInfo.displayName ?: ""
    val photoUrl = userInfo.photoUrl
    val name = displayName.ifBlank { email.substringBefore("@") }



    fun POI.getLocalizedTitle(lang: String): String {
        return when (lang) {
            "en" -> title_en
            "de" -> title_de
            "ru" -> title_ru
            else -> ""
        }.ifBlank { title }
    }

    LaunchedEffect(poi.getLocalizedTitle(currentLanguage)) {
        poiViewModel.loadWikipediaDescription(poi.getLocalizedTitle(currentLanguage))
        poiViewModel.loadReviews(poi.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = poi.getLocalizedTitle(currentLanguage),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                        },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                // IMAGE
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        poi.imageUrl.let { imageUrl ->
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
                                    .padding(8.dp),
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Visited",
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
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }

                // RATING
                item {
                    val allReviews by poiViewModel.reviews.collectAsState()
                    val reviews = allReviews[poi.id] ?: emptyList()
                    val averageRating = reviews.map { it.rating }.average().takeIf { reviews.isNotEmpty() } ?: 0.0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < averageRating.toInt()) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1f (%d)", averageRating, reviews.size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // DESCRIPTION
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = poi.getLocalizedTitle(currentLanguage),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = wikiDescription ?:
                            (when (currentLanguage) {
                                "en" -> poi.description_en
                                "de" -> poi.description_de
                                "ru" -> poi.description_ru
                                else -> ""
                            }).ifBlank { poi.description },
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 20.sp
                        )
                        distanceKm?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$it ${LocalizerUI.t(currentUnits, currentLanguage)} ${LocalizerUI.t("from", currentLanguage)} $userCurrentCity",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // MAP
                item {
                    Text(
                        text = LocalizerUI.t("onMap", currentLanguage),
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
                            title = poi.getLocalizedTitle(currentLanguage),
                        )
                    }
                }

                // REVIEWS
                item {
                    val userReviews by poiViewModel.userReviews.collectAsState()
                    val alreadyReviewed = userReviews[poi.id] == true

                    LaunchedEffect(poi.id) {
                        FirebaseAuth.getInstance().currentUser?.let {
                            poiViewModel.checkIfUserReviewed(poi.id, it.uid)
                        }
                    }

                    val reviewsMap by poiViewModel.reviews.collectAsState()
                    val poiReviews = reviewsMap[poi.id] ?: emptyList()

                    var selectedRating by remember { mutableStateOf(0) }
                    var reviewText by remember { mutableStateOf("") }
                    var showReviewForm by remember { mutableStateOf(true) }
                    val context = LocalContext.current

                    Text(
                        text = LocalizerUI.t("reviews", currentLanguage),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp)
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (poiReviews.isEmpty()) {
                            Text(
                                text = LocalizerUI.t("noReviewsYet", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            var showAllReviews by remember { mutableStateOf(false) }
                            val sortedReviews = poiReviews.sortedByDescending { it.timestamp }
                            val reviewsToDisplay = if (showAllReviews) sortedReviews else sortedReviews.take(3)

                            reviewsToDisplay.forEach { review ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!review.userPhotoUrl.isNullOrEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(review.userPhotoUrl),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color.Gray, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = review.userName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = review.userName,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold)
                                            Text(
                                                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(review.timestamp)),
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row {
                                        repeat(5) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < review.rating) Color(0xFFFFD700) else Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = review.text,
                                        color = Color.Black
                                        )
                                }
                            }

                            if (poiReviews.size > 3) {
                                TextButton(
                                    onClick = { showAllReviews = !showAllReviews },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(if (showAllReviews) LocalizerUI.t("hideReviews", currentLanguage) else "${LocalizerUI.t("showAllReviews", currentLanguage)} (${poiReviews.size - 3})")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isVisited && !alreadyReviewed) {
                            Text(
                                text = LocalizerUI.t("leaveReview", currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating $i",
                                        tint = if (i <= selectedRating) Color(0xFFFFD700) else Color.LightGray,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable { selectedRating = i }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = {
                                    if (it.length <= 500) reviewText = it
                                },
                                placeholder = { Text(LocalizerUI.t("writeReviewPlaceholder", currentLanguage)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                maxLines = 4,
                                isError = reviewText.length > 500
                            )

                            if (reviewText.length > 500) {
                                Text(
                                    text = LocalizerUI.t("reviewTooLong", currentLanguage),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user != null && reviewText.isNotBlank() && selectedRating > 0) {
                                        if (poiViewModel.hasUserReviewed(poi.id, user.uid)) {
                                            Toast.makeText(context, LocalizerUI.t("alreadyReviewed", currentLanguage), Toast.LENGTH_SHORT).show()
                                        } else {
                                            val review = Review(
                                                poiId = poi.id,
                                                userId = user.uid,
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
                                                    poiViewModel.checkIfUserReviewed(poi.id, user.uid)
                                                    Toast.makeText(context, LocalizerUI.t("reviewSubmitted", currentLanguage), Toast.LENGTH_SHORT).show()
                                                },
                                                onError = {
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                enabled = reviewText.isNotBlank() && selectedRating > 0
                            ) {
                                Text(LocalizerUI.t("submitReview", currentLanguage))
                            }
                        } else {
                            Text(
                                if (alreadyReviewed) LocalizerUI.t("alreadyReviewedText", currentLanguage) else LocalizerUI.t("reviewAfterVisit", currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Checkpoint button
            if (!isVisited) {
                Button(
                    onClick = {
                        if (isPremium) {
                            poiViewModel.markPoiVisited(poi.id)
                            pointsViewModel.addGP(100)
                            Toast.makeText(context, LocalizerUI.t("forVisit", currentLanguage), Toast.LENGTH_SHORT).show()
                        } else {
                            isChecking = true
                            coroutineScope.launch {
                                pointsViewModel.checkAndAwardGPForPOI(poi, locationViewModel) { success ->
                                    if (success) {
                                        poiViewModel.markPoiVisited(poi.id)
                                        Toast.makeText(context, LocalizerUI.t("forVisit", currentLanguage), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, LocalizerUI.t("tooFar", currentLanguage), Toast.LENGTH_SHORT).show()
                                    }
                                    isChecking = false
                                }
                            }
                        }
                    },
                    enabled = !isChecking,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isChecking) LocalizerUI.t("checking", currentLanguage) else LocalizerUI.t("imHereButton", currentLanguage),
                        color = Color.White
                    )
                }
            }

            if (isChecking) {
                LoadingOverlay(title = LocalizerUI.t("scanningArea", currentLanguage))
            }
        }
    }

}