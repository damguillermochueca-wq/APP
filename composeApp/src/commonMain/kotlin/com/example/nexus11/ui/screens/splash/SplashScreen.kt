package com.example.nexus11.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepo: AuthRepository,
    onFinish: (Boolean) -> Unit
) {

    // LÓGICA DE INICIO
    LaunchedEffect(Unit) {
        delay(2000) // Esperar 2 segundos para ver la animación

        // Comprobar si hay usuario guardado
        val userId = authRepo.getCurrentUserId()

        // Si userId NO es null -> true (Logueado)
        // Si userId ES null -> false (No logueado)
        onFinish(userId != null)
    }

    // ANIMACIÓN "LATIDO" DEL LOGO
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // UI: FONDO NEGRO PURO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBlack), // Fondo OLED
        contentAlignment = Alignment.Center
    ) {
        // 1. EL "GLOW" (Luz azul difusa detrás)
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale) // El brillo palpita
                .blur(100.dp) // Difuminado fuerte para efecto neón
                .background(NexusBlue.copy(alpha = 0.5f), CircleShape)
        )

        // 2. EL TEXTO (Blanco y Nítido)
        Text(
            text = "NEXUS",
            fontSize = 56.sp,
            fontWeight = FontWeight.Black, // Extra Negrita
            color = TextWhite,
            letterSpacing = 12.sp // Espaciado amplio "Premium"
        )
    }
}