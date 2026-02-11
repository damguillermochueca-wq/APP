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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.DataRepository
import Mensaje
import com.example.nexus11.ui.theme.NexusBlue

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        var chats by remember { mutableStateOf<List<Pair<String, Mensaje>>>(emptyList()) }
        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        LaunchedEffect(Unit) {
            chats = repo.getAllChats()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp) // 🛑 SIN PADDING EXTRA
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .background(bgColor)
            ) {
                // Cabecera pegada arriba (sin Spacer)
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                    }
                    Text("Mensajes", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                if (chats.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay conversaciones", color = textColor.copy(0.5f))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats) { (id, msg) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { /* Ir al chat */ }.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)), contentAlignment = Alignment.Center) {
                                    Text(msg.senderId.take(1).uppercase(), color = textColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("Usuario", fontWeight = FontWeight.Bold, color = textColor)
                                    Text(msg.text, color = textColor.copy(0.6f), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}