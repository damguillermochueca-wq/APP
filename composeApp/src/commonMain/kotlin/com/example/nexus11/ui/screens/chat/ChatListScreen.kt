package com.example.nexus11.ui.screens.chat

import Mensaje
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.DataRepository

import com.example.nexus11.ui.theme.NexusBlack
import com.example.nexus11.ui.theme.NexusBlue
import com.example.nexus11.ui.theme.TextGray
import com.example.nexus11.ui.theme.TextWhite

class ChatListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { DataRepository() }

        // Lista de pares: (ID del Chat, Último Mensaje)
        var listaChats by remember { mutableStateOf<List<Pair<String, Mensaje>>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            listaChats = repository.getAllChats()
            cargando = false
        }

        Scaffold(
            containerColor = NexusBlack,
            topBar = {
                TopAppBar(
                    title = { Text("Mensajes", color = TextWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = NexusBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(NexusBlack)) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NexusBlue)
                } else if (listaChats.isEmpty()) {
                    Text(
                        "No tienes conversaciones abiertas",
                        modifier = Modifier.align(Alignment.Center),
                        color = TextGray
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(listaChats) { (idDelChat, ultimoMensaje) ->
                            ItemChatLista(ultimoMensaje) {
                                // ➡️ Navegamos enviando los 3 parámetros de tu ChatDetailScreen
                                navigator.push(
                                    ChatDetailScreen(
                                        chatId = idDelChat,
                                        otherUserId = ultimoMensaje.senderId,
                                        userName = ultimoMensaje.senderId // O ultimoMensaje.senderName si lo añades
                                    )
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = TextGray.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemChatLista(mensaje: Mensaje, alHacerClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alHacerClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular con inicial
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(NexusBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mensaje.senderId.take(1).uppercase(),
                color = NexusBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mensaje.senderId, // Aquí podrías poner senderName si lo tienes
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mensaje.text,
                color = TextGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}