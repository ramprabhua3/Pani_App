package com.pani.app.presentation.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pani.app.R

private val PaniOrange = Color(0xFFE65100)

@Composable
internal fun PhoneInputContent(
    phone: String,
    isLoading: Boolean,
    error: String?,
    onPhoneChanged: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Brand header
        Text(
            text       = stringResource(R.string.app_name),
            fontSize   = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = PaniOrange
        )
        Text(
            text     = stringResource(R.string.app_tagline),
            fontSize = 15.sp,
            color    = Color.Gray
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text       = stringResource(R.string.auth_enter_phone),
            fontSize   = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF212121)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            // Country code chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.height(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = "+91", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value         = phone,
                onValueChange = onPhoneChanged,
                placeholder   = { Text(stringResource(R.string.auth_phone_hint)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboard?.hide(); onSendOtp() }
                ),
                colors  = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PaniOrange,
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                ),
                isError  = error != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        // Error label
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = { keyboard?.hide(); onSendOtp() },
            enabled  = !isLoading && phone.length == 10,
            colors   = ButtonDefaults.buttonColors(containerColor = PaniOrange),
            shape    = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color    = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text       = stringResource(R.string.auth_send_otp),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
