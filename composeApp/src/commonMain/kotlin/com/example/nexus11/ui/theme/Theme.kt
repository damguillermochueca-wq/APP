package com.example.nexus11.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.nexus11.data.AppCache

/**
 * Motor de Temas Dinámico.
 * Envuelve toda la aplicación y regenera la paleta de colores Material3
 * cada vez que cambia el estado reactivo `AppCache.themeColor`.
 */
@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 🎨 PUNTO DE ENTRADA REACTIVO:
    // Al leer directamente del StateObject global, cualquier cambio en Settings
    // provoca una recomposición en cascada de toda la UI con el nuevo color.
    val primaryColor = AppCache.themeColor

    // 1. ESQUEMA OSCURO (Estilo principal OLED)
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

    // 2. ESQUEMA CLARO (Fallback de seguridad)
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