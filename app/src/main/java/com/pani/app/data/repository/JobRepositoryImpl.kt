package com.pani.app.data.repository

import com.pani.app.data.local.db.daos.ContactRequestDao
import com.pani.app.data.local.db.daos.JobPostDao
import com.pani.app.data.local.db.entities.ContactRequestEntity
import com.pani.app.data.local.db.entities.JobPostEntity
import com.pani.app.data.remote.api.SupabaseApiService
import com.pani.app.data.remote.dto.ContactRequestDto
import com.pani.app.data.remote.dto.JobPostDto
import com.pani.app.domain.model.ContactRequest
import com.pani.app.domain.model.JobPost
import com.pani.app.domain.model.Location
import com.pani.app.domain.repository.JobRepository
import com.pani.app.util.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobPostDao: JobPostDao,
    private val contactRequestDao: ContactRequestDao,
    private val api: SupabaseApiService
) : JobRepository {

    // ── Job Posts ─────────────────────────────────────────────────────────────

    override fun getJobsByEmployer(employerId: String): Flow<List<JobPost>> =
        jobPostDao.getJobsByEmployer(employerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshJobsByEmployer(employerId: String) {
        val dtos = api.getJobsByEmployer(employerId)
        val now = System.currentTimeMillis()
        jobPostDao.upsertAll(dtos.map { it.toEntity(cachedAt = now) })
        jobPostDao.deleteStaleEntries(now - AppConstants.JOB_CACHE_TTL_MS)
    }

    override suspend fun upsertJobPost(jobPost: JobPost): JobPost {
        val saved = api.upsertJobPost(jobPost.toDto())
        val entity = saved.toEntity(cachedAt = System.currentTimeMillis())
        jobPostDao.upsert(entity)
        return entity.toDomain()
    }

    // ── Contact Requests ──────────────────────────────────────────────────────

    override suspend fun sendContactRequest(request: ContactRequest): ContactRequest {
        // Queue locally first so the action survives offline
        val localEntity = request.toEntity(
            syncStatus = ContactRequestEntity.SyncStatus.PENDING_UPLOAD
        )
        contactRequestDao.insert(localEntity)

        return try {
            val saved = api.sendContactRequest(request.toDto())
            contactRequestDao.updateSyncStatus(localEntity.id, ContactRequestEntity.SyncStatus.SYNCED)
            saved.toDomain(syncStatus = ContactRequestEntity.SyncStatus.SYNCED)
        } catch (_: Exception) {
            // PENDING_UPLOAD — WorkManager will flush when online
            localEntity.toDomain()
        }
    }

    override fun getContactRequestsByEmployer(employerId: String): Flow<List<ContactRequest>> =
        contactRequestDao.getRequestsByEmployer(employerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getContactRequestsByWorker(workerId: String): Flow<List<ContactRequest>> =
        contactRequestDao.getRequestsByWorker(workerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun syncPendingContactRequests() {
        contactRequestDao.getPendingUpload().forEach { entity ->
            try {
                api.sendContactRequest(entity.toDto())
                contactRequestDao.updateSyncStatus(entity.id, ContactRequestEntity.SyncStatus.SYNCED)
            } catch (_: Exception) {
                // Leave as PENDING_UPLOAD — WorkManager will retry
            }
        }
    }
}

// ── Mappers ───────────────────────────────────────────────────────────────────

private fun JobPostEntity.toDomain() = JobPost(
    id            = id,
    employerId    = employerId,
    title         = title,
    description   = description,
    tradeRequired = tradeRequired,
    location      = Location(latitude, longitude),
    radiusKm      = radiusKm,
    isActive      = isActive,
    postedAt      = postedAt
)

private fun JobPostDto.toEntity(cachedAt: Long) = JobPostEntity(
    id            = id ?: error("JobPostDto.id must not be null when mapping to entity"),
    employerId    = employerId,
    title         = title,
    description   = description,
    tradeRequired = tradeRequired,
    latitude      = latitude,
    longitude     = longitude,
    radiusKm      = radiusKm,
    isActive      = isActive,
    postedAt      = postedAt?.let { parseIso8601ToEpoch(it) } ?: System.currentTimeMillis(),
    cachedAt      = cachedAt
)

private fun JobPost.toDto() = JobPostDto(
    id            = id.ifEmpty { null },
    employerId    = employerId,
    title         = title,
    description   = description,
    tradeRequired = tradeRequired,
    latitude      = location.latitude,
    longitude     = location.longitude,
    radiusKm      = radiusKm,
    isActive      = isActive
)

private fun ContactRequestEntity.toDomain(
    syncStatus: String = this.syncStatus
) = ContactRequest(
    id          = id,
    employerId  = employerId,
    workerId    = workerId,
    status      = status,
    initiatedAt = initiatedAt,
    syncStatus  = syncStatus
)

private fun ContactRequest.toEntity(syncStatus: String) = ContactRequestEntity(
    id          = id.ifEmpty { UUID.randomUUID().toString() },
    employerId  = employerId,
    workerId    = workerId,
    status      = status,
    initiatedAt = initiatedAt,
    syncStatus  = syncStatus
)

private fun ContactRequest.toDto() = ContactRequestDto(
    id         = id.ifEmpty { null },
    employerId = employerId,
    workerId   = workerId,
    status     = status
)

private fun ContactRequestDto.toDomain(syncStatus: String) = ContactRequest(
    id          = id ?: error("ContactRequestDto.id must not be null"),
    employerId  = employerId,
    workerId    = workerId,
    status      = status,
    initiatedAt = initiatedAt?.let { parseIso8601ToEpoch(it) } ?: System.currentTimeMillis(),
    syncStatus  = syncStatus
)

/** Minimal ISO-8601 → epoch ms without a 3rd-party date library. */
private fun parseIso8601ToEpoch(iso: String): Long =
    runCatching {
        java.time.Instant.parse(iso).toEpochMilli()
    }.getOrDefault(System.currentTimeMillis())
