package com.pani.app.domain.repository

import com.pani.app.domain.model.ContactRequest
import com.pani.app.domain.model.JobPost
import kotlinx.coroutines.flow.Flow

interface JobRepository {

    fun getJobsByEmployer(employerId: String): Flow<List<JobPost>>

    suspend fun refreshJobsByEmployer(employerId: String)

    suspend fun upsertJobPost(jobPost: JobPost): JobPost

    suspend fun sendContactRequest(request: ContactRequest): ContactRequest

    fun getContactRequestsByEmployer(employerId: String): Flow<List<ContactRequest>>

    fun getContactRequestsByWorker(workerId: String): Flow<List<ContactRequest>>

    suspend fun syncPendingContactRequests()
}
