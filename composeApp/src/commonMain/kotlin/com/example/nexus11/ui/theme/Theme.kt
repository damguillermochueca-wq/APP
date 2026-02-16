package com.example.nexus11.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.nexus11.data.AppCache

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 🎨 LEEMOS EL COLOR DINÁMICO
    // Al ser una variable de estado en AppCache,
    // cuando cambie, toda la UI se repintará automáticamente.
    val primaryColor = AppCache.themeColor

    // 1. ESQUEMA OSCURO (Nexus Style)
    val DarkColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = TextWhite,
        secondary = primaryColor,
        onSecondary = TextWhite,
        tertiary = primaryColor,

        background = NexusBlack,
        onBackground = TextWhite,
        surface = NexusDarkGray,
        onSurface = TextWhite,

        error = ErrorRed,
        onError = NexusBlack,
        outline = TextGray
    )

    // 2. ESQUEMA CLARO (Fallback)
    val LightColorScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = TextWhite,
        background = TextWhite,
        onBackground = NexusBlack,
        surface = Color(0xFFF5F5F5),
        onSurface = NexusBlack,
        error = ErrorRed,
        onError = TextWhite,
        outline = TextGray
    )

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}