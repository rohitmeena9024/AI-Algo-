package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CryptoNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = CryptoElectricBlue,
    onPrimaryContainer = Color.White,
    secondary = CryptoBullishGreen,
    onSecondary = Color.Black,
    secondaryContainer = CryptoDarkSurfaceVariant,
    onSecondaryContainer = CryptoBullishGreen,
    tertiary = CryptoWarningGold,
    onTertiary = Color.Black,
    background = CryptoDarkBackground,
    onBackground = CryptoTextPrimary,
    surface = CryptoDarkSurface,
    onSurface = CryptoTextPrimary,
    surfaceVariant = CryptoDarkSurfaceVariant,
    onSurfaceVariant = CryptoTextSecondary,
    outline = CryptoBorder,
    error = CryptoBearishRed,
    onError = Color.White
)

@Composable
fun CryptoAiFriendTheme(
    content: @Composable () -> Unit
) {
    // For crypto trading terminals, an intentional, consistent dark theme is industry standard
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards-compatible alias if referenced elsewhere
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CryptoAiFriendTheme(content = content)
}
