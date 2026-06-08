package com.pani.app.di

import android.content.Context
import androidx.room.Room
import com.pani.app.data.local.db.PaniDatabase
import com.pani.app.data.local.db.daos.ContactRequestDao
import com.pani.app.data.local.db.daos.JobPostDao
import com.pani.app.data.local.db.daos.WorkerProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePaniDatabase(@ApplicationContext context: Context): PaniDatabase =
        Room.databaseBuilder(
            context,
            PaniDatabase::class.java,
            PaniDatabase.DB_NAME
        )
            // fallbackToDestructiveMigration only for MVP; replace with proper
            // Migration objects before any production release with real user data.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWorkerProfileDao(db: PaniDatabase): WorkerProfileDao = db.workerProfileDao()

    @Provides
    fun provideJobPostDao(db: PaniDatabase): JobPostDao = db.jobPostDao()

    @Provides
    fun provideContactRequestDao(db: PaniDatabase): ContactRequestDao = db.contactRequestDao()
}
