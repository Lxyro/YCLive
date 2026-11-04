package com.yclive.streamer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MidnightColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonEmerald,
    tertiary = ElectricViolet,
    background = MidnightDeep,
    surface = MidnightSurface,
    surfaceVariant = GlassCard,
    error = CrimsonRed,
    onPrimary = MidnightDeep,
    onSecondary = MidnightDeep,
    onBackground = TextPure,
    onSurface = TextPure
)

@Composable
fun YCLiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MidnightColorScheme,
        typography = Typography,
        content = content
    )
}
