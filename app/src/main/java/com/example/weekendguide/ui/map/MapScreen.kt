package com.example.weekendguide.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.model.POI
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.collections.forEach
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    userPOIList: List<POI>,
    userLocation: Pair<Double, Double>?,
    selectedRadius: String,
    onSelectPOI: (POI) -> Unit,
    onOpenPOIinMap: () -> Unit,
    isFavorite: (POI) -> Boolean,
    isVisited: (POI) -> Boolean,
    showLocationPanel: @Composable () -> Unit,
    showFiltersButtons: @Composable () -> Unit,
) {

    val radiusValue = when (selectedRadius) {
        "20" -> 20_000.0
        "50" -> 50_000.0
        "100" -> 100_000.0
        "200" -> 200_000.0
        "∞" -> 0.0
        else -> 200_000.0
    }
    val zoom = when (selectedRadius) {
        "20" -> 10f
        "50" -> 9f
        "100" -> 8f
        else -> 7f
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation?.let { LatLng(it.first, it.second) } ?: LatLng(51.1657, 10.4515),
            zoom
        )
    }
    LaunchedEffect(selectedRadius, userLocation) {
        val newZoom = when (selectedRadius) {
            "20" -> 10f
            "50" -> 9f
            "100" -> 8f
            else -> 7f
        }

        userLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(LatLng(it.first, it.second), newZoom),
                durationMs = 1000
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            //   properties = MapProperties(isMyLocationEnabled = true) // Кнопка где я
        ) {


            userPOIList.forEach { poi ->
                val markerColor = when {
                    isVisited(poi) -> BitmapDescriptorFactory.HUE_GREEN
                    isFavorite(poi) -> BitmapDescriptorFactory.HUE_YELLOW
                    else -> BitmapDescriptorFactory.HUE_RED
                }

                val icon = BitmapDescriptorFactory.defaultMarker(markerColor)

                Marker(
                    state = MarkerState(position = LatLng(poi.lat, poi.lng)),
                    title = poi.title,
                    icon = icon,
                    onClick = {
                        onSelectPOI(poi)
                        onOpenPOIinMap()
                        true
                    }
                )
            }

            userLocation?.let {
                Circle(
                    center = LatLng(it.first, it.second),
                    radius = radiusValue,
                    fillColor = Color.Blue.copy(alpha = 0.15f),
                    strokeColor = Color.Blue,
                    strokeWidth = 2f
                )
            }
        }

        Column (
            modifier = Modifier
                .padding(top = 40.dp, start = 4.dp, end = 4.dp)
                .fillMaxSize()
        ) {
            //Поле выбор локации
            showLocationPanel()

            Spacer(modifier = Modifier.height(4.dp))

            // кнопки фильтры
            showFiltersButtons()
        }
    }
}