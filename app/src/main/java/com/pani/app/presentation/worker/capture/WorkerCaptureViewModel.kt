package com.pani.app.presentation.worker.capture

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pani.app.data.local.preferences.PaniPreferences
import com.pani.app.data.worker.HunarUploadWorker
import com.pani.app.util.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    preferences: PaniPreferences
) : ViewModel() {

    /** The authenticated user's ID from DataStore — used as the Supabase storage path. */
    val userId: StateFlow<String?> = preferences.userId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(WorkerCaptureUiState())
    val uiState: StateFlow<WorkerCaptureUiState> = _uiState.asStateFlow()

    /**
     * Screen collects this to call recording.stop() at exactly 30 seconds.
     * Using SharedFlow (not StateFlow) so the event fires once, not on recomposition.
     */
    private val _stopRecordingEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val stopRecordingEvent = _stopRecordingEvent.asSharedFlow()

    private var countdownJob: Job? = null

    // ── Recording lifecycle ───────────────────────────────────────────────────

    fun onRecordingStarted() {
        _uiState.update {
            it.copy(
                captureState  = CaptureState.RECORDING,
                countdownSecs = AppConstants.MAX_VIDEO_DURATION_SECONDS,
                error         = null
            )
        }
        startCountdown()
    }

    /** Called from the screen when the user manually taps Stop or 30s elapses. */
    fun onRecordingStopped(videoPath: String) {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                captureState      = CaptureState.REVIEW,
                recordedVideoPath = videoPath,
                countdownSecs     = AppConstants.MAX_VIDEO_DURATION_SECONDS
            )
        }
    }

    fun onRecordingError(message: String) {
        countdownJob?.cancel()
        _uiState.update { it.copy(captureState = CaptureState.PREVIEW, error = message) }
    }

    fun onRetake() {
        _uiState.update {
            it.copy(
                captureState      = CaptureState.PREVIEW,
                recordedVideoPath = null,
                countdownSecs     = AppConstants.MAX_VIDEO_DURATION_SECONDS,
                uploadProgress    = 0f
            )
        }
    }

    fun onFlipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun onErrorDismissed() = _uiState.update { it.copy(error = null) }

    // ── Upload ────────────────────────────────────────────────────────────────

    /** Enqueues the upload WorkRequest and observes its progress / result. */
    fun onUseVideo() {
        val workerId  = userId.value ?: return
        val videoPath = _uiState.value.recordedVideoPath ?: return
        _uiState.update { it.copy(captureState = CaptureState.UPLOADING, uploadProgress = 0f) }

        val request = HunarUploadWorker.buildRequest(videoPath, workerId)
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(request.id).collect { info ->
                when (info?.state) {
                    WorkInfo.State.RUNNING ->
                        _uiState.update {
                            it.copy(uploadProgress = info.progress.getFloat(HunarUploadWorker.KEY_PROGRESS, 0f))
                        }

                    WorkInfo.State.SUCCEEDED ->
                        _uiState.update { it.copy(captureState = CaptureState.DONE, uploadProgress = 1f) }

                    WorkInfo.State.FAILED ->
                        _uiState.update {
                            it.copy(captureState = CaptureState.REVIEW, error = "Upload failed. Tap 'Use video' to retry.")
                        }

                    else -> Unit
                }
            }
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in (AppConstants.MAX_VIDEO_DURATION_SECONDS - 1) downTo 0) {
                delay(1_000L)
                _uiState.update { it.copy(countdownSecs = remaining) }
            }
            // Hard cap reached — signal screen to finalize the recording
            _stopRecordingEvent.emit(Unit)
        }
    }
}
