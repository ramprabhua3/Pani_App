package com.pani.app.domain.usecase

import com.pani.app.domain.model.ContactRequest
import com.pani.app.domain.repository.JobRepository
import com.pani.app.util.ext.PaniResult
import javax.inject.Inject

class SendContactRequestUseCase @Inject constructor(
    private val repository: JobRepository
) {
    /**
     * Queues the contact request in Room immediately (offline-safe), then
     * attempts to sync with Supabase. Returns Success in both cases — the
     * sync failure is silent and handled by WorkManager.
     */
    suspend operator fun invoke(request: ContactRequest): PaniResult<ContactRequest> =
        runCatching { repository.sendContactRequest(request) }
            .fold(
                onSuccess = { PaniResult.Success(it) },
                onFailure = { PaniResult.Error(it.message ?: "Failed to send contact request", it) }
            )
}
