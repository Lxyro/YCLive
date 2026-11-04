package com.yclive.streamer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppleIOSLightColorScheme = lightColorScheme(
    primary = DockBg,
    secondary = DockBg,
    tertiary = TextSubtle,
    background = CanvasBg,
    surface = CardSurface,
    surfaceVariant = PillBgInactive,
    error = AlertRed,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun YCLiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppleIOSLightColorScheme,
        typography = Typography,
        content = content
    )
}
