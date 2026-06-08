package com.pani.app.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "worker_profiles",
    indices = [
        Index(value = ["trade_category", "is_available"]),
        Index(value = ["latitude", "longitude"]),
        Index(value = ["cached_at"])
    ]
)
data class WorkerProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    // SHA-256 hash of raw phone — raw number never stored locally
    @ColumnInfo(name = "phone_hash")
    val phoneHash: String,

    @ColumnInfo(name = "trade_category")
    val tradeCategory: String,

    // JSON-encoded list: e.g. ["two-wheeler","AC repair"]
    @ColumnInfo(name = "trade_tags")
    val tradeTags: String = "[]",

    @ColumnInfo(name = "video_url")
    val videoUrl: String?,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    // Pre-computed at fetch time for fast offline sort; null if location unknown
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double?,

    @ColumnInfo(name = "is_available")
    val isAvailable: Boolean = true,

    @ColumnInfo(name = "verified")
    val verified: Boolean = false,

    // ISO 639-1 language code: "hi", "ta", "te", "kn", "ml", "en"
    @ColumnInfo(name = "language_pref")
    val languagePref: String = "hi",

    // Epoch ms — used for 30-minute TTL sweep
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long,

    // SYNCED | PENDING_UPLOAD | DIRTY
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.SYNCED
) {
    object SyncStatus {
        const val SYNCED = "SYNCED"
        const val PENDING_UPLOAD = "PENDING_UPLOAD"
        const val DIRTY = "DIRTY"
    }
}
