package com.example.geonotify.services.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.geonotify.data.GeofenceDatabase
import com.example.geonotify.data.GeofenceRepository
import com.example.geonotify.managers.devicecontrol.DeviceControlManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        val deviceControlManager = DeviceControlManager(context)
        val repository = GeofenceRepository(GeofenceDatabase.getDatabase(context).geofenceDao())

        for (geofence in triggeringGeofences) {
            val geofenceIdStr = geofence.requestId
            val geofenceId = geofenceIdStr.toLongOrNull() ?: continue

            CoroutineScope(Dispatchers.IO).launch {
                val geofenceEntity = repository.getGeofenceById(geofenceId) ?: return@launch
                
                val message = when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        deviceControlManager.applySettings(
                            geofenceEntity.muteMicrophone,
                            geofenceEntity.muteMedia,
                            geofenceEntity.muteNotification
                        )
                        "Entered: ${geofenceEntity.name} 📍 (Automation Applied)"
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        deviceControlManager.restoreSettings(
                            geofenceEntity.muteMicrophone,
                            geofenceEntity.muteMedia,
                            geofenceEntity.muteNotification
                        )
                        "Exited: ${geofenceEntity.name} 🚪 (Settings Restored)"
                    }
                    else -> return@launch
                }
                sendNotification(context, message)
            }
        }
    }

    private fun sendNotification(context: Context, message: String) {
        val channelId = "GEOFENCE_CHANNEL"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("GeoNotify")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(message.hashCode(), notification)
    }
}
