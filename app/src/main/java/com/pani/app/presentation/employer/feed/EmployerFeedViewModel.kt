package com.pani.app.presentation.employer.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pani.app.domain.model.Location
import com.pani.app.domain.usecase.GetNearbyWorkersUseCase
import com.pani.app.util.ext.PaniResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployerFeedViewModel @Inject constructor(
    private val getNearbyWorkers: GetNearbyWorkersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployerFeedUiState())
    val uiState: StateFlow<EmployerFeedUiState> = _uiState.asStateFlow()

    // Separate location flow so filter changes don't interrupt the collect
    private val locationFlow = MutableStateFlow<Location?>(null)

    init {
        observeWorkers()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeWorkers() {
        viewModelScope.launch {
            locationFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { location ->
                    val state = _uiState.value
                    getNearbyWorkers
                        .observe(location, state.radiusKm, state.selectedTrade)
                        .collect { result ->
                            when (result) {
                                is PaniResult.Loading ->
                                    _uiState.update { it.copy(isLoading = true, error = null) }

                                is PaniResult.Success ->
                                    _uiState.update {
                                        it.copy(workers = result.data, isLoading = false, error = null)
                                    }

                                is PaniResult.Error ->
                                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                            }
                        }
                }
        }
    }

    /** Called from the screen once ACCESS_FINE_LOCATION is granted. */
    fun onLocationGranted(latitude: Double, longitude: Double) {
        val location = Location(latitude, longitude)
        _uiState.update { it.copy(currentLocation = location) }
        locationFlow.value = location
        triggerNetworkRefresh(location)
    }

    fun onRefresh() {
        val location = _uiState.value.currentLocation ?: return
        triggerNetworkRefresh(location)
    }

    fun onTradeSelected(trade: String?) {
        _uiState.update { it.copy(selectedTrade = trade) }
        // Re-trigger observe with new filter by nudging locationFlow
        locationFlow.value?.let { locationFlow.value = it }
    }

    fun onRadiusChanged(radiusKm: Int) {
        _uiState.update { it.copy(radiusKm = radiusKm) }
        locationFlow.value?.let { locationFlow.value = it }
    }

    fun onPlayingIndexChanged(index: Int) {
        _uiState.update { it.copy(playingIndex = index) }
    }

    fun onFilterSheetOpen() = _uiState.update { it.copy(isFilterSheetOpen = true) }
    fun onFilterSheetClose() = _uiState.update { it.copy(isFilterSheetOpen = false) }
    fun onErrorDismissed() = _uiState.update { it.copy(error = null) }

    private fun triggerNetworkRefresh(location: Location) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                val state = _uiState.value
                getNearbyWorkers.refresh(location, state.radiusKm, state.selectedTrade)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
