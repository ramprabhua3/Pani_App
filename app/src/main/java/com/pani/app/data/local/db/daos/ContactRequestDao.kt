package com.pani.app.data.local.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pani.app.data.local.db.entities.ContactRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ContactRequestEntity)

    @Query("SELECT * FROM contact_requests WHERE employer_id = :employerId ORDER BY initiated_at DESC")
    fun getRequestsByEmployer(employerId: String): Flow<List<ContactRequestEntity>>

    @Query("SELECT * FROM contact_requests WHERE worker_id = :workerId ORDER BY initiated_at DESC")
    fun getRequestsByWorker(workerId: String): Flow<List<ContactRequestEntity>>

    // Fetches all unsynced items for the WorkManager upload job
    @Query("SELECT * FROM contact_requests WHERE sync_status = 'PENDING_UPLOAD'")
    suspend fun getPendingUpload(): List<ContactRequestEntity>

    @Query("UPDATE contact_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE contact_requests SET sync_status = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)
}
