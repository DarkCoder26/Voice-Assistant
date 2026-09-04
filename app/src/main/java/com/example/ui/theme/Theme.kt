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
    primary = AuraCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = AuraViolet,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF352B5B),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = AuraMagenta,
    onTertiary = Color(0xFFFFFFFF),
    background = AuraDarkBg,
    onBackground = AuraTextPrimary,
    surface = AuraDarkSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraDarkSurfaceVariant,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraDarkBorder,
    error = AuraError
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006876),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA1EFFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF5D53A5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5DEFF),
    onSecondaryContainer = Color(0xFF190663),
    tertiary = Color(0xFF980054),
    onTertiary = Color.White,
    background = AuraLightBg,
    onBackground = AuraLightTextPrimary,
    surface = AuraLightSurface,
    onSurface = AuraLightTextPrimary,
    surfaceVariant = AuraLightSurfaceVariant,
    onSurfaceVariant = AuraLightTextSecondary,
    outline = AuraLightBorder,
    error = AuraError
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Prefer Aura's signature glowing palette by default
    content: @Composable () -> Unit
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

// Backward-compatibility alias for test files
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    AuraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
