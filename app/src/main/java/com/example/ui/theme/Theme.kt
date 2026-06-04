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
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = ImmersiveOnSecondary,
    secondaryContainer = ImmersiveSecondaryContainer,
    onSecondaryContainer = ImmersiveOnSecondaryContainer,
    tertiary = ImmersiveTertiary,
    onTertiary = ImmersiveOnTertiary,
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF8C1D18),
    error = ImmersiveError,
    onError = ImmersiveOnError,
    errorContainer = ImmersiveErrorContainer,
    onErrorContainer = ImmersiveOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = ImmersiveOnSecondary,
    secondaryContainer = ImmersiveSecondaryContainer,
    onSecondaryContainer = ImmersiveOnSecondaryContainer,
    tertiary = ImmersiveTertiary,
    onTertiary = ImmersiveOnTertiary,
    background = ImmersiveBackground,
    onBackground = ImmersiveOnBackground,
    surface = Color.White,
    onSurface = ImmersiveOnBackground,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveOnSurfaceVariant,
    outline = ImmersiveOutline,
    error = ImmersiveError,
    onError = ImmersiveOnError,
    errorContainer = ImmersiveErrorContainer,
    onErrorContainer = ImmersiveOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to enforce our gorgeously crafted custom celebration palette by default!
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
