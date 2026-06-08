package com.pani.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to the `worker_profiles` table in Supabase/PostgreSQL.
 *
 * PostGIS schema on Supabase (run once in SQL editor):
 *
 *   CREATE EXTENSION IF NOT EXISTS postgis;
 *
 *   CREATE TABLE worker_profiles (
 *       id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 *       name           TEXT NOT NULL,
 *       phone_hash     TEXT NOT NULL,
 *       trade_category TEXT NOT NULL,
 *       trade_tags     JSONB DEFAULT '[]'::jsonb,
 *       video_url      TEXT,
 *       thumbnail_url  TEXT,
 *       -- Stored separately for fast JSON access
 *       latitude       FLOAT8 NOT NULL,
 *       longitude      FLOAT8 NOT NULL,
 *       -- PostGIS point — kept in sync via trigger (see below)
 *       location       GEOMETRY(Point, 4326)
 *                          GENERATED ALWAYS AS (ST_MakePoint(longitude, latitude)) STORED,
 *       is_available   BOOLEAN NOT NULL DEFAULT TRUE,
 *       verified       BOOLEAN NOT NULL DEFAULT FALSE,
 *       language_pref  TEXT NOT NULL DEFAULT 'hi',
 *       created_at     TIMESTAMPTZ DEFAULT NOW(),
 *       updated_at     TIMESTAMPTZ DEFAULT NOW()
 *   );
 *
 *   CREATE INDEX idx_wp_location  ON worker_profiles USING GIST (location);
 *   CREATE INDEX idx_wp_trade_avail ON worker_profiles (trade_category, is_available);
 *
 * The `location` column uses a GENERATED column so latitude/longitude are the
 * canonical source of truth — PostgREST only serializes lat/lon as plain
 * numbers; the geometry column is opaque to the Kotlin client and used
 * exclusively server-side in ST_DWithin / ST_Distance queries.
 */
@Serializable
data class WorkerProfileDto(
    @SerialName("id")             val id: String? = null,
    @SerialName("name")           val name: String,
    @SerialName("phone_hash")     val phoneHash: String,
    @SerialName("trade_category") val tradeCategory: String,
    @SerialName("trade_tags")     val tradeTags: List<String> = emptyList(),
    @SerialName("video_url")      val videoUrl: String? = null,
    @SerialName("thumbnail_url")  val thumbnailUrl: String? = null,
    @SerialName("latitude")       val latitude: Double,
    @SerialName("longitude")      val longitude: Double,
    // Returned only by the get_nearby_workers RPC — null on plain table reads
    @SerialName("distance_km")    val distanceKm: Double? = null,
    @SerialName("is_available")   val isAvailable: Boolean = true,
    @SerialName("verified")       val verified: Boolean = false,
    @SerialName("language_pref")  val languagePref: String = "hi",
    @SerialName("created_at")     val createdAt: String? = null,
    @SerialName("updated_at")     val updatedAt: String? = null
)
