package com.pani.app.data.local.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pani.app.data.local.db.entities.JobPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(jobs: List<JobPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(job: JobPostEntity)

    @Query("""
        SELECT * FROM job_posts
        WHERE trade_required = :trade
          AND is_active = 1
          AND latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY posted_at DESC
    """)
    fun getActiveJobsByTradeAndBounds(
        trade: String,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<JobPostEntity>>

    @Query("SELECT * FROM job_posts WHERE employer_id = :employerId ORDER BY posted_at DESC")
    fun getJobsByEmployer(employerId: String): Flow<List<JobPostEntity>>

    @Query("DELETE FROM job_posts WHERE cached_at < :cutoffMs")
    suspend fun deleteStaleEntries(cutoffMs: Long)

    @Query("DELETE FROM job_posts")
    suspend fun clearAll()
}
