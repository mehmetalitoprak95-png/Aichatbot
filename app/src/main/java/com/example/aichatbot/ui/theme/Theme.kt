package com.example.aichatbot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF7C3AED),
    surfaceVariant = Color(0xFFEEF2FF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    secondary = Color(0xFFC4B5FD)
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
