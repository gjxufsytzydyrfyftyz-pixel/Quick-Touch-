package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeDarkColorScheme =
  darkColorScheme(
    primary = PureWhite,
    onPrimary = PitchBlack,
    primaryContainer = SlateTile,
    onPrimaryContainer = PureWhite,
    secondary = SoftWhite,
    onSecondary = PitchBlack,
    secondaryContainer = SlateElevated,
    onSecondaryContainer = SoftWhite,
    tertiary = TextSecondary,
    onTertiary = PitchBlack,
    background = PitchBlack,
    onBackground = PureWhite,
    surface = SlateCard,
    onSurface = PureWhite,
    surfaceVariant = SlateTile,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
  )

@Composable
fun QuickTouchTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = MonochromeDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
