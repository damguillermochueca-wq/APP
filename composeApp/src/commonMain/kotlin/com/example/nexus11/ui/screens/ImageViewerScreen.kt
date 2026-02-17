package com.example.nexus11.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.ui.screens.home.rememberBase64Image
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * Visor de Imágenes a Pantalla Completa.
 * Utiliza el mismo mecanismo de caché que el feed para carga instantánea.
 */
data class ImageViewerScreen(val imageUrl: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { navigator.pop() }, // UX: Cerrar al tocar el fondo
            contentAlignment = Alignment.Center
        ) {
            // Intentamos recuperar de RAM primero (Base64)
            val bitmap = rememberBase64Image(imageUrl)

            if (bitmap != null) {
                Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                // Si es URL remota, usamos carga asíncrona
                KamelImage(asyncPainterResource(imageUrl), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }

            // Botón de cierre explícito
            IconButton(
                onClick = { navigator.pop() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}