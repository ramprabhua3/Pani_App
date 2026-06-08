package com.pani.app.domain.repository

import com.pani.app.domain.model.Location
import com.pani.app.domain.model.Worker
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {

    /**
     * Room is the single source of truth.
     * Emits cached workers immediately; callers must separately call
     * [refreshNearbyWorkers] to pull fresh data from the network.
     */
    fun getNearbyWorkers(
        location: Location,
        radiusKm: Int,
        trade: String? = null
    ): Flow<List<Worker>>

    /**
     * Fetches workers from the Supabase ST_DWithin RPC and writes results
     * into Room. The [getNearbyWorkers] Flow re-emits automatically.
     * Throws on network failure — callers should wrap in try/catch.
     */
    suspend fun refreshNearbyWorkers(
        location: Location,
        radiusKm: Int,
        trade: String? = null
    )

    fun getWorkerById(id: String): Flow<Worker?>

    /**
     * Upserts the worker's own profile. Returns the saved profile with its
     * server-assigned id populated.
     */
    suspend fun upsertWorkerProfile(worker: Worker): Worker

    suspend fun updateAvailability(workerId: String, isAvailable: Boolean)

    /** Called by WorkManager to flush locally queued changes when online. */
    suspend fun syncPendingChanges()
}
