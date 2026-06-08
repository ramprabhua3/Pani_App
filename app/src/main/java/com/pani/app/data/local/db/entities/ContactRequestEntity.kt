package com.pani.app.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_requests",
    indices = [
        Index(value = ["employer_id"]),
        Index(value = ["worker_id"]),
        Index(value = ["sync_status"])
    ]
)
data class ContactRequestEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "employer_id")
    val employerId: String,

    @ColumnInfo(name = "worker_id")
    val workerId: String,

    // PENDING | ACCEPTED | REJECTED | CALLED
    @ColumnInfo(name = "status")
    val status: String = ContactStatus.PENDING,

    // Epoch ms
    @ColumnInfo(name = "initiated_at")
    val initiatedAt: Long,

    // SYNCED | PENDING_UPLOAD — queued for upload when network returns
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING_UPLOAD
) {
    object ContactStatus {
        const val PENDING = "PENDING"
        const val ACCEPTED = "ACCEPTED"
        const val REJECTED = "REJECTED"
        const val CALLED = "CALLED"
    }

    object SyncStatus {
        const val SYNCED = "SYNCED"
        const val PENDING_UPLOAD = "PENDING_UPLOAD"
    }
}
