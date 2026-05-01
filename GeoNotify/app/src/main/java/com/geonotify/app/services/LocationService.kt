package com.geonotify.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.geonotify.app.data.GeofenceDatabase
import com.geonotify.app.data.GeofenceEntity
import com.geonotify.app.data.GeofenceType
import com.geonotify.app.managers.devicecontrol.DeviceControlManager
import com.geonotify.app.utils.GeofenceUtils
import com.geonotify.app.utils.NotificationHelper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var deviceControlManager: DeviceControlManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val insideGeofences = mutableSetOf<Long>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHelper = NotificationHelper(this)
        deviceControlManager = DeviceControlManager(this)
        
        startLocationServiceForeground()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    checkGeofences(currentLatLng)
                }
            }
        }
        
        requestLocationUpdates()
    }

    private fun startLocationServiceForeground() {
        val notification = createServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1001, notification)
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            stopSelf()
        }
    }

    private fun checkGeofences(currentLatLng: LatLng) {
        serviceScope.launch {
            val db = GeofenceDatabase.getDatabase(applicationContext)
            val geofences = db.geofenceDao().getAllGeofences().first()
            
            for (geofence in geofences) {
                var isInside = false
                
                if (geofence.type == GeofenceType.POLYGON && geofence.points != null) {
                    isInside = GeofenceUtils.isPointInPolygon(currentLatLng, geofence.points)
                } else if (geofence.type == GeofenceType.CIRCLE && geofence.centerLat != null && geofence.centerLng != null && geofence.radius != null) {
                    val distance = GeofenceUtils.calculateDistance(
                        currentLatLng, 
                        LatLng(geofence.centerLat, geofence.centerLng)
                    )
                    isInside = distance <= geofence.radius
                }
                
                handleTransition(geofence, isInside)
            }
        }
    }

    private fun handleTransition(geofence: GeofenceEntity, isInsideNow: Boolean) {
        val wasInside = insideGeofences.contains(geofence.id)
        
        if (isInsideNow && !wasInside) {
            insideGeofences.add(geofence.id)
            notificationHelper.sendNotification("Entered ${geofence.name}", "Automation applied.")
            
            // APPLY AUTOMATION
            deviceControlManager.applySettings(
                muteMic = geofence.muteMicrophone,
                muteMedia = geofence.muteMedia,
                muteNotif = geofence.muteNotification
            )
            
        } else if (!isInsideNow && wasInside) {
            insideGeofences.remove(geofence.id)
            notificationHelper.sendNotification("Exited ${geofence.name}", "Automation restored.")
            
            // RESTORE AUTOMATION
            deviceControlManager.restoreSettings(
                restoreMic = geofence.muteMicrophone,
                restoreMedia = geofence.muteMedia,
                restoreNotif = geofence.muteNotification
            )
        }
    }

    private fun createServiceNotification(): Notification {
        val channelId = "LOCATION_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("GeoNotify Tracking")
            .setContentText("Monitoring geofences in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}
