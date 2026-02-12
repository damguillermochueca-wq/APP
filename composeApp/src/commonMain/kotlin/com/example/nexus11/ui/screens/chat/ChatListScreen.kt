package com.example.nexus11.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import Mensaje
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.theme.NexusBlue
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.decodeBase64Bytes
import androidx.compose.foundation.Image
import com.preat.peekaboo.image.picker.toImageBitmap

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        // Usamos .parent para salir de las pestañas al navegar
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }

        var chats by remember { mutableStateOf<List<Pair<String, Mensaje>>>(emptyList()) }
        val myId = authRepo.getCurrentUserId() ?: ""

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        LaunchedEffect(Unit) {
            chats = repo.getAllChats()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .background(bgColor)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (navigator.canPop) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                        }
                    }
                    Text(
                        "Mensajes",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                if (chats.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay conversaciones", color = textColor.copy(0.5f))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats) { (chatId, lastMsg) ->
                            // Calculamos el ID del otro usuario
                            val parts = chatId.split("_")
                            val otherUserId = parts.find { it != myId } ?: parts.firstOrNull() ?: ""

                            // Renderizamos la fila, que se encargará de buscar los datos del usuario
                            ChatRowItem(
                                chatId = chatId,
                                otherUserId = otherUserId,
                                lastMsg = lastMsg,
                                myId = myId,
                                repo = repo,
                                textColor = textColor,
                                onClick = { name ->
                                    navigator.push(ChatDetailScreen(chatId, otherUserId, name))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ COMPONENTE INTELIGENTE PARA CADA FILA
@Composable
fun ChatRowItem(
    chatId: String,
    otherUserId: String,
    lastMsg: Mensaje,
    myId: String,
    repo: DataRepository,
    textColor: Color,
    onClick: (String) -> Unit
) {
    // Estado para guardar los datos del usuario real
    var otherUser by remember { mutableStateOf<User?>(null) }

    // Al cargar la fila, buscamos los datos de este usuario en la BD
    LaunchedEffect(otherUserId) {
        if (otherUserId.isNotBlank()) {
            otherUser = repo.getUser(otherUserId)
        }
    }

    val displayName = otherUser?.username ?: "Cargando..."
    val displayAvatar = otherUser?.profileImageUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(displayName) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AVATAR
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (!displayAvatar.isNullOrBlank()) {
                // Lógica de imagen (Base64 o URL)
                if (displayAvatar.startsWith("data:image")) {
                    val bitmap = remember(displayAvatar) {
                        try { displayAvatar.substringAfter(",").decodeBase64Bytes().toImageBitmap() }
                        catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    KamelImage(asyncPainterResource(displayAvatar), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            } else {
                // Si no tiene foto, mostramos inicial
                Text(
                    text = displayName.take(1).uppercase(),
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // TEXTOS
        Column {
            Text(displayName, fontWeight = FontWeight.Bold, color = textColor)
            Text(
                text = if (lastMsg.senderId == myId) "Tú: ${lastMsg.text}" else lastMsg.text,
                color = textColor.copy(0.6f),
                maxLines = 1,
                fontSize = 14.sp
            )
        }
    }
}