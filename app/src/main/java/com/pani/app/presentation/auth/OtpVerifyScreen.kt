package com.pani.app.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pani.app.R

private val PaniOrange = Color(0xFFE65100)

@Composable
internal fun OtpVerifyContent(
    phone: String,
    otp: String,
    isLoading: Boolean,
    error: String?,
    resendCooldownSecs: Int,
    onOtpChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the hidden input when the OTP screen appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        IconButton(onClick = { keyboard?.hide(); onBack() }) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                tint               = Color(0xFF212121)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text       = stringResource(R.string.auth_enter_otp),
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "Sent to +91 ${"•".repeat((phone.length - 4).coerceAtLeast(0))}${phone.takeLast(4)}",
            fontSize = 14.sp,
            color    = Color.Gray
        )

        Spacer(Modifier.height(40.dp))

        // ── 6-box OTP input ───────────────────────────────────────────────────
        // A transparent BasicTextField overlays the 6 visual boxes. It captures
        // all touch events and drives the keyboard; the boxes render the digits.
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.fillMaxWidth()
        ) {
            OtpBoxRow(otp = otp, hasError = error != null)

            // Invisible input that sits on top of the boxes, captures taps / keyboard
            BasicTextField(
                value           = otp,
                onValueChange   = onOtpChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboard?.hide(); onVerify() }
                ),
                modifier        = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .focusRequester(focusRequester)
            )
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text     = error,
                color    = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = { keyboard?.hide(); onVerify() },
            enabled  = !isLoading && otp.length == 6,
            colors   = ButtonDefaults.buttonColors(containerColor = PaniOrange),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color       = Color.White,
                    modifier    = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text       = stringResource(R.string.auth_verify),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (resendCooldownSecs > 0) {
                Text(
                    text     = stringResource(R.string.auth_resend_in, resendCooldownSecs),
                    fontSize = 14.sp,
                    color    = Color.Gray
                )
            } else {
                TextButton(onClick = onResend) {
                    Text(
                        text     = stringResource(R.string.auth_resend_otp),
                        fontSize = 14.sp,
                        color    = PaniOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpBoxRow(otp: String, hasError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(6) { index ->
            val char = otp.getOrNull(index)?.toString() ?: ""
            val isCurrent = index == otp.length
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(48.dp)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = when {
                            hasError         -> MaterialTheme.colorScheme.error
                            isCurrent        -> PaniOrange
                            char.isNotEmpty() -> Color(0xFF212121)
                            else             -> Color(0xFFBDBDBD)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text       = char,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = Color(0xFF212121)
                )
            }
        }
    }
}
