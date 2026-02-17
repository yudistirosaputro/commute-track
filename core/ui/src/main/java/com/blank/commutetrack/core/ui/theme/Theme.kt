package com.blank.commutetrack.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CommuteTrackColorScheme = darkColorScheme(
    primary = CommuteColors.NeonGreen,
    onPrimary = CommuteColors.DarkestGreen,
    primaryContainer = CommuteColors.BorderGreen,
    onPrimaryContainer = CommuteColors.NeonGreen,
    secondary = CommuteColors.SlateGreen,
    onSecondary = CommuteColors.DarkestGreen,
    secondaryContainer = CommuteColors.DarkBorder,
    onSecondaryContainer = CommuteColors.SlateGreen,
    tertiary = CommuteColors.PausedAmber,
    onTertiary = CommuteColors.DarkestGreen,
    tertiaryContainer = Color(0xFF3D2E00),
    onTertiaryContainer = CommuteColors.PausedAmber,
    background = CommuteColors.DarkestGreen,
    onBackground = Color.White,
    surface = CommuteColors.GlassyCard,
    onSurface = Color.White,
    surfaceVariant = CommuteColors.DarkSurface,
    onSurfaceVariant = CommuteColors.SlateGreen,
    outline = CommuteColors.BorderGreen,
    outlineVariant = CommuteColors.DarkBorder,
    error = CommuteColors.ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF3D0000),
    onErrorContainer = CommuteColors.ErrorRed
)

@Composable
fun CommuteTrackTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CommuteColors.DarkestGreen.toArgb()
            window.navigationBarColor = CommuteColors.DarkestGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = CommuteTrackColorScheme,
        typography = CommuteTrackTypography,
        content = content
    )
}
