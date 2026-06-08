package com.pani.app.presentation.onboarding

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pani.app.data.local.preferences.PaniPreferences
import com.pani.app.domain.model.UserMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingNavEvent {
    data class Done(val mode: UserMode) : OnboardingNavEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: PaniPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<OnboardingNavEvent>(extraBufferCapacity = 1)
    val navEvent = _navEvent.asSharedFlow()

    fun onLocaleSelected(locale: SupportedLocale) {
        _uiState.update { it.copy(selectedLocale = locale.tag) }
    }

    fun onLocaleConfirmed() {
        val tag = _uiState.value.selectedLocale
        viewModelScope.launch {
            preferences.saveLocale(tag)
            // Apply the locale immediately — AppCompatDelegate propagates it to the
            // Activity via AppLocalesStorageHelper and requests a recreate.
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            _uiState.update { it.copy(step = OnboardingStep.MODE) }
        }
    }

    fun onModeSelected(mode: UserMode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            preferences.saveUserMode(mode)
            preferences.completeOnboarding()
            _uiState.update { it.copy(isLoading = false) }
            _navEvent.emit(OnboardingNavEvent.Done(mode))
        }
    }
}
