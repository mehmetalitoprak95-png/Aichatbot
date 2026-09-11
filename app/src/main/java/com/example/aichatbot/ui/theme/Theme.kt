package com.example.aichatbot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF7C3AED),
    tertiary = Color(0xFFF59E0B),
    surfaceVariant = Color(0xFFEEF2FF),
    onSurfaceVariant = Color(0xFF1E1B4B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F172A),
    secondary = Color(0xFFC4B5FD),
    tertiary = Color(0xFFFBBF24),
    surfaceVariant = Color(0xFF1E1B4B),
    onSurfaceVariant = Color(0xFFE0E7FF)
)

@Composable
fun AIChatBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
