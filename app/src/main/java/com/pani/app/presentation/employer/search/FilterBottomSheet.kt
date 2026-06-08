package com.pani.app.presentation.employer.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pani.app.R
import com.pani.app.util.constants.AppConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedTrade: String?,
    selectedRadiusKm: Int,
    onTradeSelected: (String?) -> Unit,
    onRadiusChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local draft state — committed on Apply
    var draftTrade by remember { mutableStateOf(selectedTrade) }
    var draftRadius by remember { mutableIntStateOf(selectedRadiusKm) }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        dragHandle        = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text       = stringResource(R.string.feed_filter),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // ── Trade selector ────────────────────────────────────────────────
            Text(
                text  = stringResource(R.string.search_trade_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip to clear trade filter
                FilterChip(
                    selected = draftTrade == null,
                    onClick  = { draftTrade = null },
                    label    = { Text("All") },
                    colors   = tradeChipColors()
                )
                AppConstants.TradeCategory.ALL.forEach { trade ->
                    FilterChip(
                        selected = draftTrade == trade,
                        onClick  = { draftTrade = if (draftTrade == trade) null else trade },
                        label    = { Text(tradeLabel(trade)) },
                        colors   = tradeChipColors()
                    )
                }
            }

            // ── Radius selector ───────────────────────────────────────────────
            Text(
                text  = stringResource(R.string.search_radius_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { km ->
                    FilterChip(
                        selected = draftRadius == km,
                        onClick  = { draftRadius = km },
                        label    = { Text("${km} km") },
                        colors   = tradeChipColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Action buttons ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = {
                        draftTrade  = null
                        draftRadius = AppConstants.DEFAULT_RADIUS_KM
                    },
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text       = stringResource(R.string.search_reset),
                            fontWeight = FontWeight.Medium,
                            fontSize   = 15.sp
                        )
                    }
                }

                Surface(
                    onClick = {
                        onTradeSelected(draftTrade)
                        onRadiusChanged(draftRadius)
                        onDismiss()
                    },
                    color    = MaterialTheme.colorScheme.primary,
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text       = stringResource(R.string.search_apply),
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun tradeChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor     = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor         = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor   = MaterialTheme.colorScheme.onPrimaryContainer
)

@Composable
private fun tradeLabel(trade: String): String = when (trade) {
    AppConstants.TradeCategory.MECHANIC    -> stringResource(R.string.trade_mechanic)
    AppConstants.TradeCategory.DRIVER      -> stringResource(R.string.trade_driver)
    AppConstants.TradeCategory.RETAIL      -> stringResource(R.string.trade_retail)
    AppConstants.TradeCategory.WAREHOUSE   -> stringResource(R.string.trade_warehouse)
    AppConstants.TradeCategory.ELECTRICIAN -> stringResource(R.string.trade_electrician)
    AppConstants.TradeCategory.PLUMBER     -> stringResource(R.string.trade_plumber)
    AppConstants.TradeCategory.CARPENTER   -> stringResource(R.string.trade_carpenter)
    AppConstants.TradeCategory.DELIVERY    -> stringResource(R.string.trade_delivery)
    AppConstants.TradeCategory.SECURITY    -> stringResource(R.string.trade_security)
    AppConstants.TradeCategory.HOUSEKEEPING -> stringResource(R.string.trade_housekeeping)
    else                                   -> trade
}
