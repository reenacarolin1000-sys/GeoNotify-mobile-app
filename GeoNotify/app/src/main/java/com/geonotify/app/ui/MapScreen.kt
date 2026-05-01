package com.geonotify.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.geonotify.app.data.GeofenceEntity
import com.geonotify.app.data.GeofenceType
import com.geonotify.app.utils.NotificationHelper
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: GeofenceViewModel) {
    val context = LocalContext.current
    val geofences by viewModel.allGeofences.collectAsState()
    val notificationHelper = remember { NotificationHelper(context) }
    
    val hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(11.0168, 76.9558), 13f)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(GeofenceType.CIRCLE) }
    val tempPoints = remember { mutableStateListOf<LatLng>() }
    var geofenceName by remember { mutableStateOf("") }
    var radius by remember { mutableFloatStateOf(200f) }

    // Checkbox States
    var muteMic by remember { mutableStateOf(false) }
    var muteMedia by remember { mutableStateOf(false) }
    var muteNotif by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission),
            onMapClick = { latLng ->
                if (showAddDialog) {
                    if (selectedType == GeofenceType.CIRCLE) {
                        tempPoints.clear()
                        tempPoints.add(latLng)
                    } else {
                        tempPoints.add(latLng)
                    }
                }
            }
        ) {
            geofences.forEach { geofence ->
                if (geofence.type == GeofenceType.CIRCLE && geofence.centerLat != null && geofence.centerLng != null) {
                    Circle(
                        center = LatLng(geofence.centerLat, geofence.centerLng),
                        radius = geofence.radius?.toDouble() ?: 0.0,
                        fillColor = Color.Blue.copy(alpha = 0.3f),
                        strokeColor = Color.Blue,
                        strokeWidth = 2f
                    )
                } else if (geofence.type == GeofenceType.POLYGON && geofence.points != null) {
                    Polygon(
                        points = geofence.points,
                        fillColor = Color.Red.copy(alpha = 0.3f),
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                }
            }

            if (showAddDialog) {
                if (selectedType == GeofenceType.CIRCLE && tempPoints.isNotEmpty()) {
                    Circle(center = tempPoints[0], radius = radius.toDouble(), fillColor = Color.Green.copy(alpha = 0.3f))
                } else if (selectedType == GeofenceType.POLYGON && tempPoints.isNotEmpty()) {
                    Polygon(points = tempPoints.toList(), fillColor = Color.Green.copy(alpha = 0.3f))
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f), shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Geofences: ${geofences.size}/5", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { notificationHelper.sendNotification("System Test", "Notifications active.") }) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Blue)
                }
            }

            if (showAddDialog) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedType == GeofenceType.CIRCLE, onClick = { selectedType = GeofenceType.CIRCLE; tempPoints.clear() })
                    Text("Circle")
                    RadioButton(selected = selectedType == GeofenceType.POLYGON, onClick = { selectedType = GeofenceType.POLYGON; tempPoints.clear() })
                    Text("Polygon")
                }

                TextField(value = geofenceName, onValueChange = { geofenceName = it }, label = { Text("Geofence Name") }, modifier = Modifier.fillMaxWidth())

                if (selectedType == GeofenceType.CIRCLE) {
                    Slider(value = radius, onValueChange = { radius = it }, valueRange = 50f..1000f)
                    Text("Radius: ${radius.toInt()}m", style = MaterialTheme.typography.bodySmall)
                }

                // AUTOMATION CHECKBOXES
                Text("Automation Settings:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = muteMic, onCheckedChange = { muteMic = it })
                    Text("Mute Mic", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = muteMedia, onCheckedChange = { muteMedia = it })
                    Text("Mute Media", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = muteNotif, onCheckedChange = { muteNotif = it })
                    Text("Mute Notif", style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showAddDialog = false; tempPoints.clear() }) { Text("Cancel") }
                    Button(onClick = {
                        if (geofenceName.isNotBlank() && tempPoints.isNotEmpty()) {
                            val newGeofence = if (selectedType == GeofenceType.CIRCLE) {
                                GeofenceEntity(
                                    name = geofenceName,
                                    type = GeofenceType.CIRCLE,
                                    centerLat = tempPoints[0].latitude,
                                    centerLng = tempPoints[0].longitude,
                                    radius = radius,
                                    muteMicrophone = muteMic,
                                    muteMedia = muteMedia,
                                    muteNotification = muteNotif
                                )
                            } else {
                                GeofenceEntity(
                                    name = geofenceName,
                                    type = GeofenceType.POLYGON,
                                    points = tempPoints.toList(),
                                    muteMicrophone = muteMic,
                                    muteMedia = muteMedia,
                                    muteNotification = muteNotif
                                )
                            }
                            viewModel.insert(newGeofence)
                            showAddDialog = false
                            tempPoints.clear()
                            geofenceName = ""
                            muteMic = false; muteMedia = false; muteNotif = false
                        }
                    }) { Text("Save") }
                }
            } else {
                Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add New Geofence")
                }
            }
        }
        
        Card(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 150.dp).padding(16.dp)) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(geofences) { geofence ->
                    ListItem(
                        headlineContent = { Text(geofence.name) },
                        supportingContent = { 
                            val active = listOfNotNull(
                                if(geofence.muteMicrophone) "Mic" else null,
                                if(geofence.muteMedia) "Media" else null,
                                if(geofence.muteNotification) "Notif" else null
                            ).joinToString(", ")
                            Text(if(active.isEmpty()) "No Automation" else "Mutes: $active")
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.delete(geofence) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    )
                }
            }
        }
    }
}
