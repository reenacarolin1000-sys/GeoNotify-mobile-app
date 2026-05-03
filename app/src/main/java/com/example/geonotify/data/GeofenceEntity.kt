package com.example.geonotify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class GeofenceType {
    CIRCLE, POLYGON
}

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: GeofenceType,
    val centerLat: Double? = null,
    val centerLng: Double? = null,
    val radius: Float? = null,
    val points: List<LatLng>? = null,
    
    // New Automation Features
    val muteMicrophone: Boolean = false,
    val muteMedia: Boolean = false,
    val muteNotification: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromLatLngList(value: List<LatLng>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLatLngList(value: String?): List<LatLng>? {
        val listType = object : TypeToken<List<LatLng>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
