package com.example.nexus11.ui.screens.chat

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import Mensaje
import com.example.nexus11.ui.theme.NexusBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ChatDetailScreen(
    val chatId: String,
    val otherUserId: String,
    val userName: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        val myId = authRepo.getCurrentUserId() ?: "anon"

        var textState by remember { mutableStateOf("") }
        var listaMensajes by remember { mutableStateOf<List<Mensaje>>(emptyList()) }
        val scrollState = rememberLazyListState()

        // Colores dinámicos
        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground
        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

        // Polling simple para actualizar mensajes cada 2s
        LaunchedEffect(chatId) {
            while (true) {
                try {
                    val nuevos = repo.getMessages(chatId)
                    if (nuevos.size != listaMensajes.size) {
                        listaMensajes = nuevos
                        if (listaMensajes.isNotEmpty()) scrollState.animateScrollToItem(listaMensajes.size - 1)
                    }
                } catch (e: Exception) { }
                delay(2000)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                // Cabecera manual para control total
                Column(modifier = Modifier.background(bgColor)) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = NexusBlue)
                        }
                        Text(userName, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Respeta el topBar
                    .background(bgColor)
                    .imePadding() // Sube con teclado
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    state = scrollState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(listaMensajes) { m ->
                        BurbujaMensaje(m, esMio = m.senderId == myId, textColor, surfaceColor)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mensaje...", color = textColor.copy(0.5f)) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor.copy(0.3f),
                            unfocusedContainerColor = surfaceColor.copy(0.3f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                scope.launch {
                                    val nuevoMensaje = Mensaje(
                                        id = "",
                                        senderId = myId,
                                        text = textState,
                                        timestamp = Clock.System.now().toEpochMilliseconds()
                                    )
                                    repo.sendMessage(chatId, nuevoMensaje)
                                    textState = ""
                                    // Actualizamos localmente al instante para sensación de rapidez
                                    listaMensajes = listaMensajes + nuevoMensaje
                                    scrollState.animateScrollToItem(listaMensajes.size - 1)
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
fun BurbujaMensaje(mensaje: Mensaje, esMio: Boolean, textColor: Color, surfaceColor: Color) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (esMio) NexusBlue else surfaceColor,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (esMio) 16.dp else 4.dp, bottomEnd = if (esMio) 4.dp else 16.dp)
        ) {
            Text(
                text = mensaje.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (esMio) Color.White else textColor,
                fontSize = 15.sp
            )
        }
    }
}