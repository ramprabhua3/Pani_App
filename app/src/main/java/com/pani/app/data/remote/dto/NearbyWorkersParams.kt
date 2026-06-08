package com.pani.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Parameters passed to the Supabase RPC function `get_nearby_workers`.
 *
 * The matching SQL function on Supabase uses ST_DWithin for the radius query:
 *
 *   CREATE OR REPLACE FUNCTION get_nearby_workers(
 *       user_lat   FLOAT8,
 *       user_lon   FLOAT8,
 *       radius_m   FLOAT8,
 *       trade      TEXT DEFAULT NULL
 *   )
 *   RETURNS SETOF worker_profiles
 *   LANGUAGE sql STABLE AS $$
 *       SELECT *,
 *              ST_Distance(
 *                  location::geography,
 *                  ST_MakePoint(user_lon, user_lat)::geography
 *              ) / 1000.0 AS distance_km
 *       FROM worker_profiles
 *       WHERE is_available = TRUE
 *         AND (trade IS NULL OR trade_category = trade)
 *         AND ST_DWithin(
 *                 location::geography,
 *                 ST_MakePoint(user_lon, user_lat)::geography,
 *                 radius_m
 *             )
 *       ORDER BY distance_km ASC;
 *   $$;
 *
 * Include this SQL in your Supabase migrations before calling the RPC.
 */
@Serializable
data class NearbyWorkersParams(
    @SerialName("user_lat")  val userLat: Double,
    @SerialName("user_lon")  val userLon: Double,
    @SerialName("radius_m")  val radiusMeters: Double,
    @SerialName("trade")     val trade: String? = null
)
