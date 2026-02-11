package com.example.nexus11

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.nexus11.ui.screens.auth.SplashScreen
import com.example.nexus11.ui.theme.NexusTheme // ✅ Importamos tu Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // ✅ Usamos tu Theme aquí para que controle los colores de toda la app
    NexusTheme {
        Navigator(SplashScreen())
    }
}