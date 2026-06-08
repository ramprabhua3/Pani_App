package com.pani.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to the `job_posts` table in Supabase/PostgreSQL.
 *
 * Supabase schema:
 *
 *   CREATE TABLE job_posts (
 *       id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 *       employer_id    UUID NOT NULL REFERENCES auth.users(id),
 *       title          TEXT NOT NULL,
 *       description    TEXT,
 *       trade_required TEXT NOT NULL,
 *       latitude       FLOAT8 NOT NULL,
 *       longitude      FLOAT8 NOT NULL,
 *       location       GEOMETRY(Point, 4326)
 *                          GENERATED ALWAYS AS (ST_MakePoint(longitude, latitude)) STORED,
 *       radius_km      INT NOT NULL DEFAULT 10,
 *       is_active      BOOLEAN NOT NULL DEFAULT TRUE,
 *       posted_at      TIMESTAMPTZ DEFAULT NOW()
 *   );
 *
 *   CREATE INDEX idx_jp_location ON job_posts USING GIST (location);
 *   CREATE INDEX idx_jp_trade    ON job_posts (trade_required, is_active);
 *
 * Row-Level Security (RLS) policy to add:
 *   - Employers can INSERT/UPDATE their own rows (auth.uid() = employer_id).
 *   - Workers can SELECT active rows within their radius (enforced via RPC).
 */
@Serializable
data class JobPostDto(
    @SerialName("id")             val id: String? = null,
    @SerialName("employer_id")    val employerId: String,
    @SerialName("title")          val title: String,
    @SerialName("description")    val description: String? = null,
    @SerialName("trade_required") val tradeRequired: String,
    @SerialName("latitude")       val latitude: Double,
    @SerialName("longitude")      val longitude: Double,
    @SerialName("radius_km")      val radiusKm: Int = 10,
    @SerialName("is_active")      val isActive: Boolean = true,
    @SerialName("posted_at")      val postedAt: String? = null
)
