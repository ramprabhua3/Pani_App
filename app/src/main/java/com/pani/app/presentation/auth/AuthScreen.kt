package com.pani.app.presentation.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pani.app.domain.model.UserMode

@Composable
fun AuthScreen(
    onNewUser: () -> Unit,
    onReturningUser: (UserMode) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    // Handle navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is AuthNavEvent.ToOnboarding -> onNewUser()
                is AuthNavEvent.ToHome       -> onReturningUser(event.mode)
            }
        }
    }

    when (uiState.step) {
        AuthStep.PHONE -> PhoneInputContent(
            phone         = uiState.phone,
            isLoading     = uiState.isLoading,
            error         = uiState.error,
            onPhoneChanged = viewModel::onPhoneChanged,
            onSendOtp     = { viewModel.sendOtp(activity) }
        )

        AuthStep.OTP -> OtpVerifyContent(
            phone              = uiState.phone,
            otp                = uiState.otp,
            isLoading          = uiState.isLoading,
            error              = uiState.error,
            resendCooldownSecs = uiState.resendCooldownSecs,
            onOtpChanged       = viewModel::onOtpChanged,
            onVerify           = viewModel::verifyOtp,
            onResend           = { viewModel.resendOtp(activity) },
            onBack             = viewModel::onBackToPhone
        )
    }
}
