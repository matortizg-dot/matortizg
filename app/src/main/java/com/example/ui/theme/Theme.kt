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
    primary = Emerald80,
    secondary = Mint80,
    tertiary = Coral80,
    background = Color(0xFF111412),
    surface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFF282E2A),
    onPrimary = Color(0xFF003825),
    onSecondary = Color(0xFF00382B),
    onSurface = Color(0xFFE1E3DF),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF8CF7C6)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    secondary = Mint40,
    tertiary = Coral40,
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEBF1EC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color(0xFF191C1A),
    primaryContainer = Color(0xFF8CF7C6),
    onPrimaryContainer = Color(0xFF002114)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our cohesive botanical health palette
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
