package com.pani.app.presentation.employer.feed

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.pani.app.R
import com.pani.app.domain.model.Worker
import com.pani.app.util.constants.AppConstants

/**
 * Full-height card for one worker in the vertical feed.
 *
 * Layout:
 *   ┌─────────────────────────┐
 *   │   Video / Thumbnail     │  ← ExoPlayer (playing) or Coil thumbnail
 *   │                         │
 *   │                         │
 *   ├─ gradient overlay ──────┤
 *   │  Name · Trade · ✓       │  ← high-contrast text on dark gradient
 *   │  📍 X.X km away         │
 *   │  [📞 Call]  [✉ Message] │  ← 56dp touch targets (spec requirement)
 *   └─────────────────────────┘
 */
@Composable
fun WorkerFeedCard(
    worker: Worker,
    player: ExoPlayer,
    isPlaying: Boolean,
    onCallClick: (Worker) -> Unit,
    onMessageClick: (Worker) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // ── Media layer ───────────────────────────────────────────────────────
        if (isPlaying && worker.videoUrl != null) {
            PaniVideoPlayer(
                player    = player,
                videoUrl  = worker.videoUrl,
                isPlaying = true,
                modifier  = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model             = worker.thumbnailUrl,
                contentDescription = stringResource(R.string.a11y_worker_thumbnail, worker.name),
                contentScale      = ContentScale.Crop,
                modifier          = Modifier.fillMaxSize()
            )
        }

        // ── Sunlight-readable dark gradient overlay ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // ── Info + action layer ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name row + verified badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = worker.name,
                    color      = Color.White,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f, fill = false)
                )
                if (worker.verified) {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.feed_verified),
                        tint               = Color(0xFF4CAF50),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // Trade badge + distance
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TradeBadge(trade = worker.tradeCategory)

                worker.distanceKm?.let { dist ->
                    Row(
                        verticalAlignment  = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint               = Color.White.copy(alpha = 0.8f),
                            modifier           = Modifier.size(14.dp)
                        )
                        Text(
                            text     = stringResource(R.string.feed_km_away, dist),
                            color    = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }

                if (worker.isAvailable) {
                    AvailabilityDot()
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action buttons — 56dp minimum touch target (spec requirement)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                ActionButton(
                    icon               = Icons.Filled.Call,
                    label              = stringResource(R.string.contact_call),
                    contentDescription = stringResource(R.string.a11y_call_worker, worker.name),
                    containerColor     = Color(0xFF2E7D32),
                    onClick            = { onCallClick(worker) },
                    modifier           = Modifier.weight(1f)
                )
                ActionButton(
                    icon               = Icons.Filled.MailOutline,
                    label              = stringResource(R.string.contact_message),
                    contentDescription = stringResource(R.string.a11y_message_worker, worker.name),
                    containerColor     = Color(0xFF1565C0),
                    onClick            = { onMessageClick(worker) },
                    modifier           = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TradeBadge(trade: String) {
    Surface(
        color  = Color.White.copy(alpha = 0.2f),
        shape  = RoundedCornerShape(4.dp)
    ) {
        Text(
            text      = trade,
            color     = Color.White,
            fontSize  = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun AvailabilityDot() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Color(0xFF66BB6A))
    )
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    contentDescription: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick       = onClick,
        color         = containerColor,
        shape         = RoundedCornerShape(8.dp),
        modifier      = modifier.height(AppConstants.MIN_TOUCH_TARGET_DP.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = contentDescription,
                tint               = Color.White,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text      = label,
                color     = Color.White,
                fontSize  = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier  = Modifier.padding(start = 6.dp)
            )
        }
    }
}
