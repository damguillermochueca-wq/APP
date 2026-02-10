package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ✅ NUEVOS IMPORTS DE KAMEL (Adiós Coil)
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userIdToShow: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    val authRepo = remember { AuthRepository() }
    val dataRepo = remember { DataRepository() }
    val scope = rememberCoroutineScope()

    val myRealId = authRepo.getCurrentUserId() ?: ""
    val isMyProfile = userIdToShow == "me" || userIdToShow == myRealId

    var username by remember { mutableStateOf("Usuario Nexus") }
    var email by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userIdToShow) {
        val idBusqueda = if (userIdToShow == "me") myRealId else userIdToShow
        if (idBusqueda.isNotEmpty()) {
            val user = dataRepo.getUser(idBusqueda)
            if (user != null) {
                username = user.username
                email = user.email
                avatarUrl = user.profileImageUrl
            }
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
            }
            Text(if (isMyProfile) "Mi Perfil" else "Perfil", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (isMyProfile) {
                IconButton(onClick = { /* Ajustes */ }) {
                    Icon(Icons.Default.Settings, null, tint = TextGray)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        if (loading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NexusBlue)
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))

            // FOTO DE PERFIL
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(NexusDarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    // ✅ CAMBIO CLAVE: Usamos KamelImage con asyncPainterResource
                    KamelImage(
                        resource = asyncPainterResource(avatarUrl!!),
                        contentDescription = "Avatar de $username",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        // Opcional: puedes poner un indicador de carga específico aquí
                        onLoading = { progress -> CircularProgressIndicator(progress, color = NexusBlue) },
                        onFailure = { error ->
                            Text(username.take(1).uppercase(), fontSize = 40.sp, color = TextWhite)
                        }
                    )
                } else {
                    Text(username.take(1).uppercase(), fontSize = 40.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(username, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            if (isMyProfile) {
                Text(email, fontSize = 14.sp, color = TextGray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isMyProfile) {
                Button(
                    onClick = {
                        scope.launch {
                            authRepo.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar Sesión", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val chatId = if (myRealId < userIdToShow) "${myRealId}_${userIdToShow}" else "${userIdToShow}_${myRealId}"
                        onNavigateToChat(chatId, username)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBlue),
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enviar Mensaje", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}