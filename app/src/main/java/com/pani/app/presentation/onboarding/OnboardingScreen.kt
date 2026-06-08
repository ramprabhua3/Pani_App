package com.pani.app.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pani.app.R
import com.pani.app.domain.model.UserMode

private val PaniOrange = Color(0xFFE65100)
private val PaniGreen  = Color(0xFF2E7D32)
private val PaniBlue   = Color(0xFF1565C0)

@Composable
fun OnboardingScreen(
    onComplete: (UserMode) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is OnboardingNavEvent.Done -> onComplete(event.mode)
            }
        }
    }

    when (uiState.step) {
        OnboardingStep.LANGUAGE -> LanguagePickerContent(
            selectedLocale = uiState.selectedLocale,
            onLocaleSelected = viewModel::onLocaleSelected,
            onContinue = viewModel::onLocaleConfirmed
        )

        OnboardingStep.MODE -> ModeSelectContent(
            isLoading = uiState.isLoading,
            onModeSelected = viewModel::onModeSelected
        )
    }
}

// ── Language picker ───────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerContent(
    selectedLocale: String,
    onLocaleSelected: (SupportedLocale) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = stringResource(R.string.onboarding_select_language),
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121)
        )

        Spacer(Modifier.height(32.dp))

        LazyVerticalGrid(
            columns             = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.fillMaxWidth()
        ) {
            items(SupportedLocale.entries) { locale ->
                val selected = locale.tag == selectedLocale
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) PaniOrange else Color(0xFFBDBDBD),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (selected) PaniOrange.copy(alpha = 0.08f) else Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onLocaleSelected(locale) }
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = locale.displayName,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (selected) PaniOrange else Color(0xFF212121),
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            text      = locale.nativeName,
                            fontSize  = 11.sp,
                            color     = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        Button(
            onClick  = onContinue,
            colors   = ButtonDefaults.buttonColors(containerColor = PaniOrange),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text       = stringResource(R.string.onboarding_continue),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Mode selection ────────────────────────────────────────────────────────────

@Composable
private fun ModeSelectContent(
    isLoading: Boolean,
    onModeSelected: (UserMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = stringResource(R.string.onboarding_select_mode),
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121),
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        if (isLoading) {
            CircularProgressIndicator(color = PaniOrange)
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModeCard(
                    icon        = Icons.Filled.Person,
                    label       = stringResource(R.string.onboarding_mode_worker),
                    tint        = PaniGreen,
                    onClick     = { onModeSelected(UserMode.WORKER) },
                    modifier    = Modifier.weight(1f)
                )
                ModeCard(
                    icon        = Icons.Filled.Business,
                    label       = stringResource(R.string.onboarding_mode_employer),
                    tint        = PaniBlue,
                    onClick     = { onModeSelected(UserMode.EMPLOYER) },
                    modifier    = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.height(160.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text       = label,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = tint,
                textAlign  = TextAlign.Center
            )
        }
    }
}
