package com.pani.app.util.ext

import com.pani.app.domain.model.Location
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.asin

data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
)

/**
 * Computes an approximate bounding box around [this] for a given [radiusKm].
 * Used to pre-filter Room's geo index before the Haversine sort.
 *
 * Accuracy is sufficient for 5–15 km radii in South India (near equator).
 */
fun Location.toBoundingBox(radiusKm: Double): BoundingBox {
    val latDelta = radiusKm / 111.0
    val lonDelta = radiusKm / (111.0 * cos(Math.toRadians(latitude)))
    return BoundingBox(
        minLat = latitude - latDelta,
        maxLat = latitude + latDelta,
        minLon = longitude - lonDelta,
        maxLon = longitude + lonDelta
    )
}

/** Haversine distance in km between two points. */
fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
