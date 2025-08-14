package com.example.weekendguide.ui.poi

import android.content.ContentValues
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.data.model.Review
import com.example.weekendguide.data.model.UserData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIFullScreen(
    poi: POI,
    isFavorite: Boolean,
    isVisited: Boolean,
    userData: UserData,
    onFavoriteClick: () -> Unit,
    userLocation: Pair<Double, Double>? = null,
    userCurrentCity: String? = null,
    onDismiss: () -> Unit,
    poiViewModel: POIViewModel,
    pointsViewModel: PointsViewModel,
    locationViewModel: LocationViewModel,
    loginViewModel: LoginViewModel,
    currentLanguage: String,
    currentUnits: String,
    tagsIcons: Map<String, ImageVector>,
    typeIcons: Map<String, ImageVector>,
) {
    val distanceKm = remember(poi, userLocation) {
        userLocation?.let { (userLat, userLng) ->
            val result = FloatArray(1)
            Location.distanceBetween(userLat, userLng, poi.lat, poi.lng, result)
            (result[0] / 1000).toInt()
        }
    }

    val isTest = userData.test_mode != false

    val context = LocalContext.current
    val wikiDescription by poiViewModel.wikiDescription.collectAsState()
    val wikiAnnotatedDescription by poiViewModel.wikiAnnotatedDescription.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    val userInfo by loginViewModel.userData.collectAsState()
    val email = userInfo.email ?: ""
    val displayName = userInfo.displayName ?: ""
    val photoUrl = userInfo.photoUrl
    val name = displayName.ifBlank { email.substringBefore("@") }

    val localizedTitle =
        when (currentLanguage) {
            "en" -> poi.title_en
            "de" -> poi.title_de
            "ru" -> poi.title_ru
            else -> poi.title
        }.ifBlank { poi.title }

    val localizedDescription = wikiDescription?.takeIf { it.isNotBlank() }
        ?: when (currentLanguage) {
            "en" -> poi.description_en
            "de" -> poi.description_de
            "ru" -> poi.description_ru
            else -> poi.description
        }.ifBlank { poi.description }
            .ifBlank {
                LocalizerUI.t("desc_type_${poi.type}", currentLanguage)
            }

    suspend fun sharePOI() {
        val title = localizedTitle
        val description = localizedDescription
            .lines()
            .joinToString("\n") { it.trimStart() }
            .trim()
        val locationUrl = "https://maps.google.com/?q=${poi.lat},${poi.lng}"

        val shareText = buildString {
            append("📍 $title\n")
            append(description.trim())
            append("\n\n🤳 ${LocalizerUI.t("share_by", currentLanguage)}\n\n")
            append("🌍 $locationUrl")
        }

        val imageUri = withContext(Dispatchers.IO) {
            poi.imageUrl?.let { imageUrl ->
                try {
                    val url = URL(imageUrl)
                    val inputStream = url.openStream()
                    val file = File(context.cacheDir, "shared_image.jpg")
                    file.outputStream().use { inputStream.copyTo(it) }
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = if (imageUri != null) "image/*" else "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            imageUri?.let {
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        context.startActivity(Intent.createChooser(intent, "Share to"))
    }

    fun saveAsGpx(): Boolean {
        return try {
            val gpxContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="WeekendGuide" xmlns="http://www.topografix.com/GPX/1/1">
                <wpt lat="${poi.lat}" lon="${poi.lng}">
                    <name>${poi.title}</name>
                </wpt>
            </gpx>
        """.trimIndent()

            val fileName = "poi_${poi.id}.gpx"

            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/gpx+xml")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI.let {
                    resolver.insert(it, contentValues)
                }

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(gpxContent.toByteArray())
                    }
                    Toast.makeText(context, "Saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
                    return true
                } else {
                    Toast.makeText(context, "Failed to save GPX file", Toast.LENGTH_SHORT).show()
                    return false
                }

            } else {
                // Android 7–9 (API 24–28)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                file.writeText(gpxContent)
                Toast.makeText(context, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving GPX file", Toast.LENGTH_SHORT).show()
            return false
        }
    }


    LaunchedEffect(localizedTitle) {
        poiViewModel.loadWikipediaDescription(localizedTitle)
        poiViewModel.loadReviews(poi.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedTitle,
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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

                // RATING & SHARE_BUTTON & DOWNLOAD_GPX_BUTTON
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

                        Spacer(modifier = Modifier.width(8.dp))

                        //shareButton
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    sharePOI()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        //downloadGPXButton
                        IconButton(onClick = {
                            saveAsGpx()
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Save as GPX")
                        }
                    }
                }

                // TYPE & TAGS
                item {
                    FlowRow(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typeIcon = typeIcons[poi.type] ?: Icons.Default.Place
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = poi.type,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = LocalizerUI.t(poi.type, currentLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        )

                        poi.tags.distinct().forEach { tag ->
                            val tagIcon = tagsIcons[tag] ?: Icons.AutoMirrored.Filled.Label
                            FilterChip(
                                selected = false,
                                onClick = { },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = tagIcon,
                                            contentDescription = tag,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = LocalizerUI.t(tag, currentLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // TITLE & DESCRIPTION
                item {
                    Column(modifier = Modifier.padding(16.dp)) {

                        //title
                        Text(
                            text = localizedTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        //desc
                        wikiAnnotatedDescription?.let { annotatedText ->
                            ClickableText(
                                text = annotatedText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations("URL", offset, offset)
                                        .firstOrNull()?.let { annotation ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                            context.startActivity(intent)
                                        }
                                }
                            )
                        } ?: Text(
                            text = localizedDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        //distance
                        distanceKm?.let {
                            val distance = if (currentUnits == "mi") round(it * 0.621371).toInt() else it
                            Text(
                                text = "$distance ${LocalizerUI.t(currentUnits, currentLanguage)} ${LocalizerUI.t("from", currentLanguage)} $userCurrentCity",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
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
                            title = localizedTitle,
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
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!review.userPhotoUrl.isNullOrEmpty()) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(review.userPhotoUrl),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
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

                                                Spacer(modifier = Modifier.width(12.dp))

                                                // Имя, дата и звезды в одной колонке
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = review.userName,
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
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
                                                    }

                                                    Text(
                                                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(review.timestamp)),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = review.text,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
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
                        if (isTest) {
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )

                    )
                }
            }

            if (isChecking) {
                LoadingOverlay(title = LocalizerUI.t("scanningArea", currentLanguage))
            }
        }
    }

}