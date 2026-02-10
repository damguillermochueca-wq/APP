package com.example.nexus11.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. ESQUEMA OSCURO (Tu prioridad: OLED)
private val DarkColorScheme = darkColorScheme(
    // Colores Principales
    primary = NexusBlue,            // Botones, Switches, Títulos activos
    onPrimary = TextWhite,          // Texto sobre el azul (Blanco)

    secondary = NexusBlue,          // Usamos el mismo azul para coherencia
    onSecondary = TextWhite,

    // Fondos y Superficies
    background = NexusBlack,        // ✅ El negro puro (000000)
    onBackground = TextWhite,       // Texto blanco sobre fondo negro

    surface = NexusDarkGray,        // ✅ Tarjetas y Menús (1C1C1E)
    onSurface = TextWhite,          // Texto blanco sobre tarjetas grises

    // Errores y Bordes
    error = ErrorRed,
    onError = NexusBlack,

    outline = TextGray,             // Bordes de inputs inactivos
    outlineVariant = NexusDarkGray  // Líneas separadoras sutiles
)

// 2. ESQUEMA CLARO (Inversión segura)
// Aunque tu app es "Dark First", esto asegura que no explote si el móvil está en modo claro
private val LightColorScheme = lightColorScheme(
    primary = NexusBlue,
    onPrimary = TextWhite,

    secondary = NexusBlue,
    onSecondary = TextWhite,

    background = TextWhite,         // Fondo blanco (usando tu variable TextWhite como fondo aquí)
    onBackground = NexusBlack,      // Texto negro (usando tu NexusBlack como texto aquí)

    surface = Color(0xFFF5F5F5),    // Un gris muy clarito estándar para diferenciar tarjetas
    onSurface = NexusBlack,

    error = ErrorRed,
    onError = TextWhite,

    outline = TextGray
)

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Si el sistema está en oscuro, usa tu paleta OLED. Si no, la clara.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}