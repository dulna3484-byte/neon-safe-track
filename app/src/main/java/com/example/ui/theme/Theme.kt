package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SafeEmerald500,
    onPrimary = Color.White,
    primaryContainer = SafeEmerald700,
    onPrimaryContainer = SafeEmeraldLight,
    secondary = SafeCyan500,
    onSecondary = Color.White,
    secondaryContainer = SafeNavy700,
    onSecondaryContainer = SafeCyanLight,
    tertiary = SafeAlertAmber,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SafeNavy700,
    onSurfaceVariant = TextSecondaryDark,
    error = SafeAlertRed,
    errorContainer = SafeAlertRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = SafeNavy800,
    onPrimary = Color.White,
    primaryContainer = SafeNavy700,
    onPrimaryContainer = Color.White,
    secondary = SafeEmerald600,
    onSecondary = Color.White,
    secondaryContainer = SafeEmeraldLight,
    onSecondaryContainer = SafeEmerald700,
    tertiary = SafeCyan600,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    error = SafeAlertRed,
    errorContainer = SafeAlertRedLight
)

@Composable
fun SafeTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    SafeTrackTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
