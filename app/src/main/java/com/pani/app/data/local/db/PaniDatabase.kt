package com.pani.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pani.app.data.local.db.daos.ContactRequestDao
import com.pani.app.data.local.db.daos.JobPostDao
import com.pani.app.data.local.db.daos.WorkerProfileDao
import com.pani.app.data.local.db.entities.ContactRequestEntity
import com.pani.app.data.local.db.entities.JobPostEntity
import com.pani.app.data.local.db.entities.WorkerProfileEntity

@Database(
    entities = [
        WorkerProfileEntity::class,
        JobPostEntity::class,
        ContactRequestEntity::class
    ],
    version = 1,
    exportSchema = true  // schema exported to /schemas for migration tracking
)
abstract class PaniDatabase : RoomDatabase() {
    abstract fun workerProfileDao(): WorkerProfileDao
    abstract fun jobPostDao(): JobPostDao
    abstract fun contactRequestDao(): ContactRequestDao

    companion object {
        const val DB_NAME = "pani_cache.db"
    }
}
