package com.geonotify.app.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeofenceUtils {

    /**
     * Point-in-Polygon algorithm using Ray Casting.
     */
    fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var intersectCount = 0
        for (j in polygon.indices) {
            val i = if (j == 0) polygon.size - 1 else j - 1
            val vi = polygon[i]
            val vj = polygon[j]
            if (((vj.latitude > point.latitude) != (vi.latitude > point.latitude)) &&
                (point.longitude < (vi.longitude - vj.longitude) * (point.latitude - vj.latitude) / (vi.latitude - vj.latitude) + vj.longitude)
            ) {
                intersectCount++
            }
        }
        return intersectCount % 2 != 0
    }

    /**
     * Calculate distance between two points in meters.
     */
    fun calculateDistance(p1: LatLng, p2: LatLng): Float {
        val r = 6371e3 // Earth radius in meters
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (r * c).toFloat()
    }
}
