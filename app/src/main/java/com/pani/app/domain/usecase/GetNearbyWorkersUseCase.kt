package com.pani.app.domain.usecase

import com.pani.app.domain.model.Location
import com.pani.app.domain.model.Worker
import com.pani.app.domain.repository.WorkerRepository
import com.pani.app.util.constants.AppConstants
import com.pani.app.util.ext.PaniResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetNearbyWorkersUseCase @Inject constructor(
    private val repository: WorkerRepository
) {
    /**
     * Returns a Flow that emits cached workers immediately (PaniResult.Loading
     * on start, then PaniResult.Success from Room).
     *
     * Call [refresh] separately to pull fresh data from the Supabase PostGIS
     * RPC and write it into Room — the Flow will re-emit automatically.
     */
    fun observe(
        location: Location,
        radiusKm: Int = AppConstants.DEFAULT_RADIUS_KM,
        trade: String? = null
    ): Flow<PaniResult<List<Worker>>> =
        repository.getNearbyWorkers(location, radiusKm, trade)
            .map<List<Worker>, PaniResult<List<Worker>>> { PaniResult.Success(it) }
            .onStart { emit(PaniResult.Loading) }
            .catch { e -> emit(PaniResult.Error(e.message ?: "Failed to load workers", e)) }

    /** Triggers a network fetch; throws on failure so the ViewModel can surface the error. */
    suspend fun refresh(
        location: Location,
        radiusKm: Int = AppConstants.DEFAULT_RADIUS_KM,
        trade: String? = null
    ) = repository.refreshNearbyWorkers(location, radiusKm, trade)
}
