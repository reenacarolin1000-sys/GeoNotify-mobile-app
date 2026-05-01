package com.geonotify.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geonotify.app.data.GeofenceEntity
import com.geonotify.app.data.GeofenceRepository
import com.geonotify.app.data.GeofenceType
import com.geonotify.app.services.geofence.GeofenceHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GeofenceViewModel(
    private val repository: GeofenceRepository,
    private val geofenceHelper: GeofenceHelper
) : ViewModel() {

    val allGeofences: StateFlow<List<GeofenceEntity>> = repository.allGeofences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(geofence: GeofenceEntity) {
        viewModelScope.launch {
            repository.insert(geofence)
            // Register with Android System if it's a CIRCLE (Geofencing API only supports circles natively)
            if (geofence.type == GeofenceType.CIRCLE && geofence.centerLat != null && geofence.centerLng != null && geofence.radius != null) {
                geofenceHelper.addGeofence(geofence.centerLat, geofence.centerLng, geofence.radius)
            }
        }
    }

    fun update(geofence: GeofenceEntity) {
        viewModelScope.launch {
            repository.update(geofence)
        }
    }

    fun delete(geofence: GeofenceEntity) {
        viewModelScope.launch {
            repository.delete(geofence)
        }
    }
}

class GeofenceViewModelFactory(
    private val repository: GeofenceRepository,
    private val geofenceHelper: GeofenceHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeofenceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeofenceViewModel(repository, geofenceHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
