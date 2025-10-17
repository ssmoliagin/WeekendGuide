package com.weekendguide.app.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.weekendguide.app.data.model.POI
import com.weekendguide.app.viewmodel.MarkerIconViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import android.graphics.Color as AndroidColor

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
    markerIconViewModel: MarkerIconViewModel
) {
    val context = LocalContext.current

    val mapView = remember {
        com.google.android.gms.maps.MapView(context).apply {
            onCreate(null)
            onResume()
        }
    }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var clusterManager by remember { mutableStateOf<ClusterManager<POIClusterItem>?>(null) }

    val radiusMeters = when (selectedRadius) {
        "15", "25" -> 25000.0
        "30", "50" -> 50000.0
        "60", "100" -> 100000.0
        "120", "200" -> 200000.0
        "∞" -> 0.0
        else -> 200000.0
    }

    val zoomLevel = when (selectedRadius) {
        "15", "25" -> 10f
        "30", "50" -> 9f
        "60", "100" -> 8f
        else -> 7f
    }

    LaunchedEffect(Unit) {
        markerIconViewModel.prepareDescriptorsIfNeeded(context)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { gMap ->
                    googleMap = gMap

                    clusterManager = ClusterManager(context, gMap)
                    clusterManager?.renderer = object : DefaultClusterRenderer<POIClusterItem>(context, gMap, clusterManager) {
                        override fun onBeforeClusterItemRendered(item: POIClusterItem, markerOptions: MarkerOptions) {
                            val poi = item.poi
                            val descriptor = markerIconViewModel.getDescriptorSync(poi.type, isVisited(poi), isFavorite(poi))
                            if (descriptor != null) {
                                markerOptions.icon(descriptor)
                            } else {
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            }
                            markerOptions.title(poi.title)
                        }
                    }

                    clusterManager?.setOnClusterClickListener { cluster ->
                        val zoom = gMap.cameraPosition.zoom + 2f
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.position, zoom))
                        true
                    }

                    clusterManager?.setOnClusterItemClickListener { item ->
                        onSelectPOI(item.poi)
                        onOpenPOIinMap()
                        true
                    }

                    gMap.setOnCameraIdleListener(clusterManager)
                    gMap.setOnMarkerClickListener(clusterManager)

                    clusterManager?.clearItems()
                    clusterManager?.addItems(userPOIList.map { POIClusterItem(it) })
                    clusterManager?.cluster()

                    userLocation?.let {
                        val latLng = LatLng(it.first, it.second)
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
                    }
                }
            }
        },
        update = {
            googleMap?.let { gMap ->
                clusterManager?.let { cm ->
                    gMap.clear()

                    cm.clearItems()
                    cm.addItems(userPOIList.map { POIClusterItem(it) })
                    cm.cluster()

                    userLocation?.let { loc ->
                        val latLng = LatLng(loc.first, loc.second)
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
                    }

                    if (radiusMeters > 0 && userLocation != null) {
                        gMap.addCircle(
                            CircleOptions()
                                .center(LatLng(userLocation.first, userLocation.second))
                                .radius(radiusMeters)
                                .strokeColor(AndroidColor.BLUE)
                                .fillColor(AndroidColor.argb(0x22, 0x00, 0x00, 0xFF))
                                .strokeWidth(3f)
                        )
                    }
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(top = 40.dp, start = 4.dp, end = 4.dp)
                .fillMaxSize()
        ) {
            showLocationPanel()
            Spacer(modifier = Modifier.height(4.dp))
            showFiltersButtons()
        }
    }
}

// Cluster Google Map
class POIClusterItem(val poi: POI) : com.google.maps.android.clustering.ClusterItem {
    override fun getPosition(): LatLng = LatLng(poi.lat, poi.lng)
    override fun getTitle(): String? = poi.title
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float = 0f
}

