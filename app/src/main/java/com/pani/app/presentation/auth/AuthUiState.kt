package com.pani.app.presentation.auth

enum class AuthStep { PHONE, OTP }

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE,
    val phone: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Seconds remaining before "Resend OTP" is enabled. 0 = can resend now. */
    val resendCooldownSecs: Int = 0
)
