package com.pani.app.presentation.employer.feed

import com.pani.app.domain.model.Location
import com.pani.app.domain.model.Worker
import com.pani.app.util.constants.AppConstants

data class EmployerFeedUiState(
    val workers: List<Worker> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTrade: String? = null,
    val radiusKm: Int = AppConstants.DEFAULT_RADIUS_KM,
    val currentLocation: Location? = null,
    val isFilterSheetOpen: Boolean = false,
    /** Index of the worker whose video is currently playing in the feed. */
    val playingIndex: Int = 0
)
