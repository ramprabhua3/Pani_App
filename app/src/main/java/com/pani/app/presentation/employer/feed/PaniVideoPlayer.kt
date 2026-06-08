package com.pani.app.presentation.employer.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Lifecycle-aware ExoPlayer wrapper for the vertical feed.
 *
 * Pass the [player] instance from the parent screen — one shared player
 * handles the whole feed, minimising memory and decoder overhead on
 * low-end devices. The [videoUrl] prop drives which media item is active;
 * changing it re-loads the player without releasing/re-creating it.
 */
@Composable
fun PaniVideoPlayer(
    player: ExoPlayer,
    videoUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var isBuffering by remember { mutableStateOf(true) }

    // Swap media item when the visible URL changes
    LaunchedEffect(videoUrl) {
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.prepare()
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    // Track buffering state for the loading indicator
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    // Pause on lifecycle stop (app backgrounded / screen off)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> player.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) player.play()
                else                      -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false        // custom controls on the card
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view -> view.player = player },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }

        if (!isPlaying && !isBuffering) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/** Creates a single ExoPlayer that is released when the composition leaves. */
@Composable
fun rememberPaniPlayer(): ExoPlayer {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(0)
            .setSeekBackIncrementMs(0)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE    // loop each 30s clip
                volume = 1f
            }
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    return player
}
