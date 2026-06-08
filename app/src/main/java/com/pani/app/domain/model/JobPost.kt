package com.pani.app.domain.model

data class JobPost(
    val id: String,
    val employerId: String,
    val title: String,
    val description: String?,
    val tradeRequired: String,
    val location: Location,
    val radiusKm: Int,
    val isActive: Boolean,
    val postedAt: Long
)
