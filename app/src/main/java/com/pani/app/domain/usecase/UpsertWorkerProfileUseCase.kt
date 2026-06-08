package com.pani.app.domain.usecase

import com.pani.app.domain.model.Worker
import com.pani.app.domain.repository.WorkerRepository
import com.pani.app.util.ext.PaniResult
import javax.inject.Inject

class UpsertWorkerProfileUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    suspend operator fun invoke(worker: Worker): PaniResult<Worker> =
        runCatching { repository.upsertWorkerProfile(worker) }
            .fold(
                onSuccess = { PaniResult.Success(it) },
                onFailure = { PaniResult.Error(it.message ?: "Failed to save profile", it) }
            )
}
