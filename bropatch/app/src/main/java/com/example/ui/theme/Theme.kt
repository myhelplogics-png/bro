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
    primary = HighDensityAccentLight,
    onPrimary = HighDensityOnPrimaryContainer,
    primaryContainer = HighDensityPrimary,
    onPrimaryContainer = Color.White,
    secondary = HighDensitySecondaryContainer,
    onSecondary = HighDensityOnSecondaryContainer,
    secondaryContainer = HighDensitySecondary,
    onSecondaryContainer = Color.White,
    tertiary = HighDensityTertiaryContainer,
    onTertiary = HighDensityOnTertiaryContainer,
    background = Color(0xFF1D1B20),
    onBackground = Color(0xFFE6E1E5),
    surface = HighDensityDarkCard,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondary = HighDensitySecondary,
    onSecondary = Color.White,
    secondaryContainer = HighDensitySecondaryContainer,
    onSecondaryContainer = HighDensityOnSecondaryContainer,
    tertiary = HighDensityTertiary,
    onTertiary = Color.White,
    tertiaryContainer = HighDensityTertiaryContainer,
    onTertiaryContainer = HighDensityOnTertiaryContainer,
    background = HighDensityBg,
    onBackground = HighDensityText,
    surface = HighDensitySurface,
    onSurface = HighDensityText,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityMutedText,
    outline = HighDensityOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature Bropatch brand styling
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
