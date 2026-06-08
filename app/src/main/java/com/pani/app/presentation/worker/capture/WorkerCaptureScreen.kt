package com.pani.app.presentation.worker.capture

import android.Manifest
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pani.app.R
import com.pani.app.util.constants.AppConstants
import java.io.File

@Composable
fun WorkerCaptureScreen(
    onUploadComplete: () -> Unit,
    viewModel: WorkerCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Active Recording handle — kept in compose state so we can stop it
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    // ── Camera permission ─────────────────────────────────────────────────────
    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions[Manifest.permission.CAMERA] == true &&
                             permissions[Manifest.permission.RECORD_AUDIO] == true
    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    // ── Stop recording when ViewModel signals 30s elapsed ────────────────────
    LaunchedEffect(Unit) {
        viewModel.stopRecordingEvent.collect {
            activeRecording?.stop()
            activeRecording = null
        }
    }

    // ── Navigate away when upload is done ─────────────────────────────────────
    LaunchedEffect(uiState.captureState) {
        if (uiState.captureState == CaptureState.DONE) onUploadComplete()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        when (uiState.captureState) {

            // ── PREVIEW / RECORDING ───────────────────────────────────────────
            CaptureState.PREVIEW, CaptureState.RECORDING -> {
                if (permissionsGranted) {
                    CameraPreview(
                        isFrontCamera      = uiState.isFrontCamera,
                        onVideoCaptureReady = { vc -> videoCapture = vc },
                        modifier           = Modifier.fillMaxSize()
                    )
                }

                // Top bar — flip camera button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (uiState.captureState == CaptureState.PREVIEW) {
                        IconButton(
                            onClick  = viewModel::onFlipCamera,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Cameraswitch,
                                contentDescription = "Flip camera",
                                tint               = Color.White,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Bottom controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Countdown label
                    if (uiState.captureState == CaptureState.RECORDING) {
                        Text(
                            text       = "${uiState.countdownSecs}s",
                            color      = Color.White,
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text     = stringResource(R.string.capture_tap_to_start),
                            color    = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }

                    // Record button with countdown arc ring
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.captureState == CaptureState.RECORDING) {
                            val progress = uiState.countdownSecs.toFloat() / AppConstants.MAX_VIDEO_DURATION_SECONDS
                            Canvas(modifier = Modifier.size(88.dp)) {
                                // Background ring
                                drawArc(
                                    color       = Color.White.copy(alpha = 0.3f),
                                    startAngle  = -90f,
                                    sweepAngle  = 360f,
                                    useCenter   = false,
                                    style       = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                                    topLeft     = Offset(5.dp.toPx(), 5.dp.toPx()),
                                    size        = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
                                )
                                // Countdown arc (shrinks as time runs out)
                                drawArc(
                                    color       = Color(0xFFFF5252),
                                    startAngle  = -90f,
                                    sweepAngle  = progress * 360f,
                                    useCenter   = false,
                                    style       = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                                    topLeft     = Offset(5.dp.toPx(), 5.dp.toPx()),
                                    size        = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                when (uiState.captureState) {
                                    CaptureState.PREVIEW   -> startRecording(
                                        context, videoCapture,
                                        onStarted = viewModel::onRecordingStarted,
                                        onFinalized = { path -> viewModel.onRecordingStopped(path) },
                                        onError = { msg -> viewModel.onRecordingError(msg) }
                                    ).also { rec -> activeRecording = rec }

                                    CaptureState.RECORDING -> {
                                        activeRecording?.stop()
                                        activeRecording = null
                                    }

                                    else -> Unit
                                }
                            },
                            shape  = CircleShape,
                            color  = if (uiState.captureState == CaptureState.RECORDING)
                                         Color(0xFFFF5252) else Color.White,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (uiState.captureState == CaptureState.RECORDING)
                                        Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                                    contentDescription = if (uiState.captureState == CaptureState.RECORDING)
                                        "Stop recording" else "Start recording",
                                    tint     = if (uiState.captureState == CaptureState.RECORDING)
                                                   Color.White else Color(0xFFFF5252),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── REVIEW ────────────────────────────────────────────────────────
            CaptureState.REVIEW -> {
                VideoReviewContent(
                    videoPath  = uiState.recordedVideoPath ?: "",
                    onRetake   = viewModel::onRetake,
                    onUseVideo = viewModel::onUseVideo
                )
            }

            // ── UPLOADING ─────────────────────────────────────────────────────
            CaptureState.UPLOADING -> {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text      = stringResource(R.string.capture_uploading),
                        color     = Color.White,
                        fontSize  = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress         = { uiState.uploadProgress },
                        modifier         = Modifier.fillMaxWidth(0.7f),
                        color            = Color(0xFF66BB6A),
                        trackColor       = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text    = "${(uiState.uploadProgress * 100).toInt()}%",
                        color   = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            CaptureState.DONE -> Unit // LaunchedEffect navigates away
        }
    }
}

// ── Video review composable ───────────────────────────────────────────────────

@Composable
private fun VideoReviewContent(
    videoPath: String,
    onRetake: () -> Unit,
    onUseVideo: () -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoPath))
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false } },
            modifier = Modifier.fillMaxSize()
        )

        // Action buttons at the bottom
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick  = onRetake,
                color    = Color.White.copy(alpha = 0.2f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text       = "Retake",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                }
            }

            Surface(
                onClick  = onUseVideo,
                color    = Color(0xFF2E7D32),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text       = "Use this video",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        }
    }
}

// ── Recording helper — keeps CameraX callbacks out of the composable body ────

private fun startRecording(
    context: android.content.Context,
    videoCapture: VideoCapture<Recorder>?,
    onStarted: () -> Unit,
    onFinalized: (String) -> Unit,
    onError: (String) -> Unit
): Recording? {
    videoCapture ?: return null

    val outputFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
        "hunar_${System.currentTimeMillis()}.mp4"
    )
    val outputOptions = FileOutputOptions.Builder(outputFile).build()

    return try {
        videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start    -> onStarted()
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            onError("Recording error: ${event.error}")
                        } else {
                            onFinalized(outputFile.absolutePath)
                        }
                    }
                    else -> Unit
                }
            }
    } catch (e: SecurityException) {
        onError("Audio permission required")
        null
    }
}
