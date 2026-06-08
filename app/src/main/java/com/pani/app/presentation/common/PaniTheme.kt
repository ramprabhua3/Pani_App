package com.pani.app.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors — high contrast for sunlight readability
private val PaniOrange = Color(0xFFE65100)
private val PaniAmber  = Color(0xFFFFA000)
private val PaniGreen  = Color(0xFF2E7D32)
private val PaniBlue   = Color(0xFF1565C0)

private val LightColors = lightColorScheme(
    primary          = PaniOrange,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFDDB3),
    secondary        = PaniGreen,
    onSecondary      = Color.White,
    tertiary         = PaniBlue,
    background       = Color(0xFFFFFBF7),
    surface          = Color.White,
    error            = Color(0xFFB71C1C)
)

private val DarkColors = darkColorScheme(
    primary          = PaniAmber,
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF7F4200),
    secondary        = Color(0xFF66BB6A),
    onSecondary      = Color.Black,
    tertiary         = Color(0xFF90CAF9),
    background       = Color(0xFF1A1A1A),
    surface          = Color(0xFF2C2C2C),
    error            = Color(0xFFEF9A9A)
)

@Composable
fun PaniTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}
