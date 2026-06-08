package com.pani.app.domain.model

data class Worker(
    val id: String,
    val name: String,
    val tradeCategory: String,
    val tradeTags: List<String>,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val location: Location,
    /** Pre-computed by the PostGIS RPC and stored in cache; null for self-profile reads. */
    val distanceKm: Double?,
    val isAvailable: Boolean,
    val verified: Boolean,
    val languagePref: String,
    val syncStatus: String
)
