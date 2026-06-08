package com.pani.app.presentation.employer.feed

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.pani.app.R
import com.pani.app.domain.model.Worker
import com.pani.app.presentation.employer.search.FilterBottomSheet
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerFeedScreen(
    onCallWorker: (Worker) -> Unit,
    onMessageWorker: (Worker) -> Unit,
    viewModel: EmployerFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val player = rememberPaniPlayer()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // ── Location permission ───────────────────────────────────────────────────
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { viewModel.onLocationGranted(it.latitude, it.longitude) }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ── Auto-play: track which item is most visible ───────────────────────────
    val playingIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index -> viewModel.onPlayingIndexChanged(index) }
    }

    // ── Error snackbar ────────────────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::onRefresh,
            modifier     = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.workers.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.workers.isEmpty() -> {
                    Text(
                        text     = stringResource(R.string.feed_empty),
                        style    = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        state    = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = uiState.workers,
                            key   = { _, worker -> worker.id }
                        ) { index, worker ->
                            WorkerFeedCard(
                                worker        = worker,
                                player        = player,
                                isPlaying     = index == playingIndex && worker.videoUrl != null,
                                onCallClick   = onCallWorker,
                                onMessageClick = onMessageWorker,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .size(screenHeightDp)   // one card = one screen height
                            )
                        }
                    }
                }
            }
        }

        // ── Transparent top bar (filter icon always accessible) ───────────────
        TopAppBar(
            title = {
                Text(
                    text  = stringResource(R.string.feed_title),
                    color = Color.White
                )
            },
            actions = {
                IconButton(
                    onClick  = viewModel::onFilterSheetOpen,
                    modifier = Modifier.size(56.dp)     // 56dp touch target
                ) {
                    Icon(
                        imageVector        = Icons.Filled.FilterList,
                        contentDescription = stringResource(R.string.feed_filter),
                        tint               = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.systemBarsPadding()
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Filter bottom sheet ───────────────────────────────────────────────────
    if (uiState.isFilterSheetOpen) {
        FilterBottomSheet(
            selectedTrade    = uiState.selectedTrade,
            selectedRadiusKm = uiState.radiusKm,
            onTradeSelected  = viewModel::onTradeSelected,
            onRadiusChanged  = viewModel::onRadiusChanged,
            onDismiss        = viewModel::onFilterSheetClose
        )
    }
}
