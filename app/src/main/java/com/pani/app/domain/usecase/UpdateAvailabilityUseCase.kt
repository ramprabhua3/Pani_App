package com.pani.app.domain.usecase

import com.pani.app.domain.repository.WorkerRepository
import com.pani.app.util.ext.PaniResult
import javax.inject.Inject

class UpdateAvailabilityUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    /**
     * Optimistically updates Room first, then syncs to Supabase.
     * On network failure the local change persists with DIRTY status
     * and WorkManager retries it when connectivity returns.
     */
    suspend operator fun invoke(workerId: String, isAvailable: Boolean): PaniResult<Unit> =
        runCatching { repository.updateAvailability(workerId, isAvailable) }
            .fold(
                onSuccess = { PaniResult.Success(Unit) },
                onFailure = { PaniResult.Error(it.message ?: "Failed to update availability", it) }
            )
}
