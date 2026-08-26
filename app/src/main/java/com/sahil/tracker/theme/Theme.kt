package com.sahil.tracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF6C63FF)
private val PurpleVariant = Color(0xFF9C59F5)
private val Teal = Color(0xFF00BFA5)

private val DarkColors = darkColorScheme(
    primary = Purple,
    secondary = Teal,
    tertiary = Color(0xFFFF6D00),
    background = Color(0xFF0F0F14),
    surface = Color(0xFF1A1A24),
    surfaceVariant = Color(0xFF22223A),
    onBackground = Color(0xFFF0F0FF),
    onSurface = Color(0xFFF0F0FF),
    onSurfaceVariant = Color(0xFF9090B0)
)

private val LightColors = lightColorScheme(
    primary = Purple,
    secondary = Teal,
    tertiary = Color(0xFFFF6D00),
    background = Color(0xFFF5F5FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEFD),
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF6060A0)
)

@Composable
fun TypingTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
