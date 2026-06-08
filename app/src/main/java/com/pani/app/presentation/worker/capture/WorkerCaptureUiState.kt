package com.pani.app.presentation.worker.capture

import com.pani.app.util.constants.AppConstants

enum class CaptureState {
    /** Camera preview is live; not recording. */
    PREVIEW,
    /** Recording in progress; countdown is ticking. */
    RECORDING,
    /** Recording complete; reviewing the clip before upload. */
    REVIEW,
    /** Uploading to Supabase Storage via WorkManager. */
    UPLOADING,
    /** Upload confirmed — WorkManager result received. */
    DONE
}

data class WorkerCaptureUiState(
    val captureState: CaptureState = CaptureState.PREVIEW,
    val isFrontCamera: Boolean = false,
    val countdownSecs: Int = AppConstants.MAX_VIDEO_DURATION_SECONDS,
    /** Absolute path to the recorded MP4 file in app-external storage. */
    val recordedVideoPath: String? = null,
    /** 0.0 – 1.0 progress from WorkManager observer. */
    val uploadProgress: Float = 0f,
    val error: String? = null
)
