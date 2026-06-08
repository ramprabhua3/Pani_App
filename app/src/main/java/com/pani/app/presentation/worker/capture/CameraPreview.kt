package com.pani.app.presentation.worker.capture

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pani.app.util.constants.AppConstants
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * CameraX preview surface bound to the Compose lifecycle.
 *
 * [onVideoCaptureReady] is called once the [VideoCapture] use case is
 * bound and ready — the screen uses this reference to start/stop recording.
 *
 * Recording quality: HD (1280×720) with a target bitrate of
 * [AppConstants.VIDEO_ENCODING_BITRATE] (2 Mbps H.264). CameraX applies
 * this at record time so no post-processing transcode is required.
 */
@Composable
fun CameraPreview(
    isFrontCamera: Boolean,
    onVideoCaptureReady: (VideoCapture<Recorder>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = remember { Preview.Builder().build() }

    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HD,
                    androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .setTargetVideoEncodingBitRate(AppConstants.VIDEO_ENCODING_BITRATE)
            .build()
    }
    val videoCapture: VideoCapture<Recorder> = remember { VideoCapture.withOutput(recorder) }

    // Re-bind when camera facing changes (front ↔ back)
    LaunchedEffect(isFrontCamera) {
        val cameraProvider = context.awaitCameraProvider()
        val selector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
            onVideoCaptureReady(videoCapture)
        } catch (e: Exception) {
            // Camera binding failed — propagated via onVideoCaptureReady not being called;
            // the screen's error state will surface this.
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { view ->
                view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                preview.setSurfaceProvider(view.surfaceProvider)
            }
        },
        modifier = modifier
    )
}

/** Suspend helper — awaits the [ProcessCameraProvider] without blocking the main thread. */
private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            { cont.resume(future.get()) },
            ContextCompat.getMainExecutor(this)
        )
    }
