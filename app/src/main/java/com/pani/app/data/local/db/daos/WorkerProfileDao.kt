package com.pani.app.data.local.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pani.app.data.local.db.entities.WorkerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(workers: List<WorkerProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(worker: WorkerProfileEntity)

    // Bounding-box pre-filter; Haversine sorting is applied in the repository layer
    @Query("""
        SELECT * FROM worker_profiles
        WHERE trade_category = :trade
          AND is_available = 1
          AND latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY distance_km ASC
    """)
    fun getWorkersByTradeAndBounds(
        trade: String,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM worker_profiles WHERE id = :id")
    fun getWorkerById(id: String): Flow<WorkerProfileEntity?>

    @Query("SELECT * FROM worker_profiles WHERE sync_status != 'SYNCED'")
    suspend fun getPendingSync(): List<WorkerProfileEntity>

    // TTL sweep — delete entries older than :cutoffMs (30 min = 1_800_000 ms)
    @Query("DELETE FROM worker_profiles WHERE cached_at < :cutoffMs")
    suspend fun deleteStaleEntries(cutoffMs: Long)

    @Update
    suspend fun update(worker: WorkerProfileEntity)

    @Query("UPDATE worker_profiles SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM worker_profiles")
    suspend fun clearAll()
}
