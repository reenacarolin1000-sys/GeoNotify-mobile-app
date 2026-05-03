package com.example.geonotify.services.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*

class GeofenceHelper(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    @SuppressLint("MissingPermission")
    fun addGeofence(id: Long, lat: Double, lng: Double, radius: Float) {

        val geofence = Geofence.Builder()
            .setRequestId(id.toString())
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER
            )
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d("GEOFENCE", "Geofence $id Added Successfully")
            }
            .addOnFailureListener {
                Log.e("GEOFENCE", "Failed to add geofence $id: ${it.message}")
            }
    }

    fun removeGeofence(id: Long) {
        geofencingClient.removeGeofences(listOf(id.toString()))
            .addOnSuccessListener {
                Log.d("GEOFENCE", "Geofence $id Removed Successfully")
            }
            .addOnFailureListener {
                Log.e("GEOFENCE", "Failed to remove geofence $id: ${it.message}")
            }
    }
}
