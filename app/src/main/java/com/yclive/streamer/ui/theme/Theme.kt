package com.yclive.streamer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeLiquidColorScheme = darkColorScheme(
    primary = PureWhite,
    secondary = PureWhite,
    tertiary = MutedSlate,
    background = ObsidianBlack,
    surface = SurfaceDark,
    surfaceVariant = GlassCard,
    error = AlertRed,
    onPrimary = ObsidianBlack,
    onSecondary = ObsidianBlack,
    onBackground = PureWhite,
    onSurface = PureWhite
)

@Composable
fun YCLiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonochromeLiquidColorScheme,
        typography = Typography,
        content = content
    )
}
