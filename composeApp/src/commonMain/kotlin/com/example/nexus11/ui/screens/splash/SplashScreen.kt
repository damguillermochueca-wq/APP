package com.example.nexus11.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.ui.screens.MainScreen
import com.example.nexus11.ui.theme.NexusBlack
import com.example.nexus11.ui.theme.NexusBlue
import kotlinx.coroutines.delay

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { AuthRepository() }

        LaunchedEffect(Unit) {
            delay(2000)

            // ✅ LEEMOS EL ID DEL ALMACENAMIENTO PERSISTENTE
            val userId = authRepository.getCurrentUserId()

            if (!userId.isNullOrBlank()) {
                navigator.replaceAll(MainScreen())
            } else {
                navigator.replaceAll(LoginScreen())
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(NexusBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "NEXUS 11", color = NexusBlue, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
    }
}