package com.pani.app.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_posts",
    indices = [
        Index(value = ["trade_required", "is_active"]),
        Index(value = ["latitude", "longitude"]),
        Index(value = ["cached_at"])
    ]
)
data class JobPostEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "employer_id")
    val employerId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "trade_required")
    val tradeRequired: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "radius_km")
    val radiusKm: Int = 10,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    // Epoch ms
    @ColumnInfo(name = "posted_at")
    val postedAt: Long,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long
)
