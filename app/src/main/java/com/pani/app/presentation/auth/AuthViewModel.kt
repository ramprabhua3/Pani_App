package com.pani.app.presentation.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.pani.app.data.local.preferences.PaniPreferences
import com.pani.app.domain.model.UserMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

sealed interface AuthNavEvent {
    data object ToOnboarding : AuthNavEvent
    data class ToHome(val mode: UserMode) : AuthNavEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferences: PaniPreferences
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<AuthNavEvent>(extraBufferCapacity = 1)
    val navEvent = _navEvent.asSharedFlow()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var resendJob: Job? = null

    init {
        checkExistingSession()
    }

    // ── Session check ─────────────────────────────────────────────────────────

    private fun checkExistingSession() {
        viewModelScope.launch {
            val storedUserId = preferences.userId.first()
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null && storedUserId != null) {
                val onboardingDone = preferences.onboardingComplete.first()
                if (onboardingDone) {
                    val mode = preferences.userMode.first() ?: UserMode.EMPLOYER
                    _navEvent.emit(AuthNavEvent.ToHome(mode))
                } else {
                    _navEvent.emit(AuthNavEvent.ToOnboarding)
                }
            }
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    fun onPhoneChanged(phone: String) =
        _uiState.update { it.copy(phone = phone.filter { c -> c.isDigit() }.take(10), error = null) }

    fun onOtpChanged(otp: String) =
        _uiState.update { it.copy(otp = otp.filter { c -> c.isDigit() }.take(6), error = null) }

    // ── OTP send / resend ─────────────────────────────────────────────────────

    /**
     * [activity] is required by Firebase for reCAPTCHA verification.
     * It is passed as a method parameter (not stored) to avoid memory leaks.
     */
    fun sendOtp(activity: Activity) {
        val phone = _uiState.value.phone.trim()
        if (phone.length != 10) {
            _uiState.update { it.copy(error = "Enter a valid 10-digit number") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        buildAndSendOtp("+91$phone", activity, resendToken)
    }

    fun resendOtp(activity: Activity) {
        if (_uiState.value.resendCooldownSecs > 0) return
        val phone = _uiState.value.phone
        _uiState.update { it.copy(isLoading = true, error = null) }
        buildAndSendOtp("+91$phone", activity, resendToken)
    }

    private fun buildAndSendOtp(
        e164Phone: String,
        activity: Activity,
        forceToken: PhoneAuthProvider.ForceResendingToken?
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieved OTP (rare on real devices) — sign in immediately
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Verification failed") }
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = verId
                resendToken = token
                _uiState.update {
                    it.copy(
                        isLoading         = false,
                        step              = AuthStep.OTP,
                        resendCooldownSecs = 60
                    )
                }
                startResendCooldown()
            }
        }

        val builder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(e164Phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceToken != null) builder.setForceResendingToken(forceToken)

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    // ── OTP verify ────────────────────────────────────────────────────────────

    fun verifyOtp() {
        val verId = verificationId
        if (verId == null) {
            _uiState.update { it.copy(error = "Session expired. Please re-enter your number.") }
            return
        }
        val otp = _uiState.value.otp.trim()
        if (otp.length != 6) {
            _uiState.update { it.copy(error = "Enter the 6-digit OTP") }
            return
        }
        signInWithCredential(PhoneAuthProvider.getCredential(verId, otp))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: error("Sign-in returned null user")
                val isNewUser   = result.additionalUserInfo?.isNewUser == true

                // Store Firebase UID as the app's user identifier.
                // TODO: exchange for a Supabase session via signInWith(IDToken) once
                // Firebase is configured as an OIDC provider in the Supabase dashboard.
                preferences.saveUserId(firebaseUser.uid)

                _uiState.update { it.copy(isLoading = false) }
                if (isNewUser || !preferences.onboardingComplete.first()) {
                    _navEvent.emit(AuthNavEvent.ToOnboarding)
                } else {
                    val mode = preferences.userMode.first() ?: UserMode.EMPLOYER
                    _navEvent.emit(AuthNavEvent.ToHome(mode))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Sign-in failed. Try again.")
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun startResendCooldown() {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (remaining in 59 downTo 0) {
                delay(1_000L)
                _uiState.update { it.copy(resendCooldownSecs = remaining) }
            }
        }
    }

    fun onErrorDismissed() = _uiState.update { it.copy(error = null) }

    fun onBackToPhone() {
        resendJob?.cancel()
        _uiState.update {
            it.copy(step = AuthStep.PHONE, otp = "", error = null, resendCooldownSecs = 0)
        }
    }
}
