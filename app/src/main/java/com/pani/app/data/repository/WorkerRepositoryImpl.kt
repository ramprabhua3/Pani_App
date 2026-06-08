package com.pani.app.data.repository

import com.pani.app.data.local.db.daos.WorkerProfileDao
import com.pani.app.data.local.db.entities.WorkerProfileEntity
import com.pani.app.data.remote.api.SupabaseApiService
import com.pani.app.data.remote.dto.WorkerProfileDto
import com.pani.app.domain.model.Location
import com.pani.app.domain.model.Worker
import com.pani.app.domain.repository.WorkerRepository
import com.pani.app.util.constants.AppConstants
import com.pani.app.util.ext.toBoundingBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val dao: WorkerProfileDao,
    private val api: SupabaseApiService
) : WorkerRepository {

    // ── Offline-first feed ────────────────────────────────────────────────────

    override fun getNearbyWorkers(
        location: Location,
        radiusKm: Int,
        trade: String?
    ): Flow<List<Worker>> {
        val box = location.toBoundingBox(radiusKm.toDouble())
        val rawFlow = if (trade != null) {
            dao.getWorkersByTradeAndBounds(trade, box.minLat, box.maxLat, box.minLon, box.maxLon)
        } else {
            dao.getAllWorkersByBounds(box.minLat, box.maxLat, box.minLon, box.maxLon)
        }
        return rawFlow.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshNearbyWorkers(
        location: Location,
        radiusKm: Int,
        trade: String?
    ) {
        val dtos = api.getNearbyWorkers(location.latitude, location.longitude, radiusKm, trade)
        val now = System.currentTimeMillis()
        dao.upsertAll(dtos.map { it.toEntity(cachedAt = now) })
        dao.deleteStaleEntries(now - AppConstants.WORKER_CACHE_TTL_MS)
    }

    // ── Single profile ────────────────────────────────────────────────────────

    override fun getWorkerById(id: String): Flow<Worker?> =
        dao.getWorkerById(id).map { it?.toDomain() }

    override suspend fun upsertWorkerProfile(worker: Worker): Worker {
        val saved = api.upsertWorkerProfile(worker.toDto())
        val entity = saved.toEntity(
            cachedAt   = System.currentTimeMillis(),
            syncStatus = WorkerProfileEntity.SyncStatus.SYNCED
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun updateAvailability(workerId: String, isAvailable: Boolean) {
        // Optimistic local write — UI is instant on volatile 4G
        dao.updateAvailabilityLocal(workerId, isAvailable, WorkerProfileEntity.SyncStatus.DIRTY)
        try {
            api.updateAvailability(workerId, isAvailable)
            dao.updateSyncStatus(workerId, WorkerProfileEntity.SyncStatus.SYNCED)
        } catch (_: Exception) {
            // DIRTY status remains; WorkManager will retry when online
        }
    }

    // ── WorkManager sync ──────────────────────────────────────────────────────

    override suspend fun syncPendingChanges() {
        dao.getPendingSync().forEach { entity ->
            try {
                api.upsertWorkerProfile(entity.toDto())
                dao.updateSyncStatus(entity.id, WorkerProfileEntity.SyncStatus.SYNCED)
            } catch (_: Exception) {
                // Leave status as DIRTY — WorkManager will retry
            }
        }
    }
}

// ── Mappers (private to this file — translation boundary stays in data layer) ──

private fun WorkerProfileEntity.toDomain() = Worker(
    id            = id,
    name          = name,
    tradeCategory = tradeCategory,
    tradeTags     = runCatching { Json.decodeFromString<List<String>>(tradeTags) }
                        .getOrDefault(emptyList()),
    videoUrl      = videoUrl,
    thumbnailUrl  = thumbnailUrl,
    location      = Location(latitude, longitude),
    distanceKm    = distanceKm,
    isAvailable   = isAvailable,
    verified      = verified,
    languagePref  = languagePref,
    syncStatus    = syncStatus
)

private fun WorkerProfileDto.toEntity(
    cachedAt: Long,
    syncStatus: String = WorkerProfileEntity.SyncStatus.SYNCED
) = WorkerProfileEntity(
    id            = id ?: error("WorkerProfileDto.id must not be null when mapping to entity"),
    name          = name,
    phoneHash     = phoneHash,
    tradeCategory = tradeCategory,
    tradeTags     = Json.encodeToString(tradeTags),
    videoUrl      = videoUrl,
    thumbnailUrl  = thumbnailUrl,
    latitude      = latitude,
    longitude     = longitude,
    distanceKm    = distanceKm,
    isAvailable   = isAvailable,
    verified      = verified,
    languagePref  = languagePref,
    cachedAt      = cachedAt,
    syncStatus    = syncStatus
)

private fun WorkerProfileEntity.toDto() = WorkerProfileDto(
    id            = id,
    name          = name,
    phoneHash     = phoneHash,
    tradeCategory = tradeCategory,
    tradeTags     = runCatching { Json.decodeFromString<List<String>>(tradeTags) }
                        .getOrDefault(emptyList()),
    videoUrl      = videoUrl,
    thumbnailUrl  = thumbnailUrl,
    latitude      = latitude,
    longitude     = longitude,
    distanceKm    = distanceKm,
    isAvailable   = isAvailable,
    verified      = verified,
    languagePref  = languagePref
)

private fun Worker.toDto() = WorkerProfileDto(
    id            = id,
    name          = name,
    phoneHash     = "",   // Phone hash is set server-side on first auth — never round-tripped
    tradeCategory = tradeCategory,
    tradeTags     = tradeTags,
    videoUrl      = videoUrl,
    thumbnailUrl  = thumbnailUrl,
    latitude      = location.latitude,
    longitude     = location.longitude,
    isAvailable   = isAvailable,
    verified      = verified,
    languagePref  = languagePref
)
