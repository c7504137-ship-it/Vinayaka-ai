package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val VinayakaColorScheme = darkColorScheme(
    primary = SleekLightBlue,
    secondary = SleekViolet,
    tertiary = SleekPink,
    background = SleekBlack,
    surface = SleekCharcoal,
    surfaceVariant = SleekCharcoal,
    onPrimary = SleekBlack,
    onSecondary = PureWhite,
    onTertiary = SleekBlack,
    onBackground = SleekTextMain,
    onSurface = SleekTextMain,
    onSurfaceVariant = SleekTextMuted,
    error = SystemRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VinayakaColorScheme,
        typography = Typography,
        content = content
    )
}
