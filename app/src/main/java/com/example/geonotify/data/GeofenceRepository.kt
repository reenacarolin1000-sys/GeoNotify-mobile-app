package com.example.geonotify.data

import kotlinx.coroutines.flow.Flow

class GeofenceRepository(private val geofenceDao: GeofenceDao) {
    val allGeofences: Flow<List<GeofenceEntity>> = geofenceDao.getAllGeofences()

    suspend fun insert(geofence: GeofenceEntity): Long {
        return geofenceDao.insertGeofence(geofence)
    }

    suspend fun update(geofence: GeofenceEntity) {
        geofenceDao.updateGeofence(geofence)
    }

    suspend fun delete(geofence: GeofenceEntity) {
        geofenceDao.deleteGeofence(geofence)
    }

    suspend fun getGeofenceById(id: Long): GeofenceEntity? {
        return geofenceDao.getGeofenceById(id)
    }
}
