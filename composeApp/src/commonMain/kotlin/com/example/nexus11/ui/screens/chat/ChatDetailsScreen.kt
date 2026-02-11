package com.example.nexus11.ui.screens.chat

import Mensaje
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ChatDetailScreen(
    val chatId: String,
    val otherUserId: String,
    val userName: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        val myId = authRepo.getCurrentUserId() ?: "anon"

        var textState by remember { mutableStateOf("") }
        var listaMensajes by remember { mutableStateOf<List<Mensaje>>(emptyList()) } // ✅ Clase Mensaje
        val scrollState = rememberLazyListState()

        // Bucle para recibir mensajes nuevos
        LaunchedEffect(chatId) {
            while (true) {
                try {
                    val nuevos = repo.getMessages(chatId) // Asegúrate que en repo devuelva List<Mensaje>
                    if (nuevos.size != listaMensajes.size) {
                        listaMensajes = nuevos
                        if (listaMensajes.isNotEmpty()) {
                            scrollState.animateScrollToItem(listaMensajes.size - 1)
                        }
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
                delay(2000)
            }
        }

        Scaffold(
            containerColor = NexusBlack,
            topBar = {
                TopAppBar(
                    title = { Text(userName, color = TextWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = NexusBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().background(NexusBlack)) {

                // Área de mensajes
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                    state = scrollState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(listaMensajes) { m ->
                        BurbujaMensaje(m, esMio = m.senderId == myId)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Input de texto
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding().imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mensaje...", color = TextGray) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NexusDarkGray,
                            unfocusedContainerColor = NexusDarkGray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = NexusBlue,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                scope.launch {
                                    val nuevoMensaje = Mensaje( // ✅ Clase Mensaje
                                        senderId = myId,
                                        text = textState,
                                        timestamp = Clock.System.now().toEpochMilliseconds(),
                                        status = "sent"
                                    )
                                    repo.sendMessage(chatId, nuevoMensaje)
                                    textState = ""
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp).background(NexusBlue, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BurbujaMensaje(mensaje: Mensaje, esMio: Boolean) { // ✅ Clase Mensaje
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (esMio) NexusBlue else NexusDarkGray,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esMio) 16.dp else 2.dp,
                bottomEnd = if (esMio) 2.dp else 16.dp
            )
        ) {
            Text(
                text = mensaje.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = TextWhite,
                fontSize = 15.sp
            )
        }
    }
}