package com.pani.app.domain.model

data class ContactRequest(
    val id: String,
    val employerId: String,
    val workerId: String,
    val status: String,
    val initiatedAt: Long,
    val syncStatus: String
)
