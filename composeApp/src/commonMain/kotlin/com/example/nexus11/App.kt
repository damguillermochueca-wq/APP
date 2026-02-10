package com.example.nexus11

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.nexus11.ui.screens.splash.SplashScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Arrancamos siempre por el Splash
        Navigator(SplashScreen())
    }
}