package com.example.nexus11

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // ✅ Asegúrate de tener esta importación

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Esto obliga a la app a usar CUALQUIER tamaño de pantalla de arriba a abajo
        enableEdgeToEdge()

        setContent {
            App() // Tu función principal de Compose
        }
    }
}