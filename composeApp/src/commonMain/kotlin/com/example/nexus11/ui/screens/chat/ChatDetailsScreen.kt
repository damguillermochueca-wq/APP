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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Message
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock // ✅ IMPORTANTE: La clase correcta para KMP

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

        var messageText by remember { mutableStateOf("") }
        var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
        val listState = rememberLazyListState()

        LaunchedEffect(chatId) {
            while (true) {
                val newMessages = repo.getMessages(chatId)
                if (newMessages.size != messages.size) {
                    messages = newMessages
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
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
                        // Este SÍ lleva botón atrás para volver a las pestañas
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = NexusBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    state = listState
                ) {
                    items(messages) { msg ->
                        MessageBubble(msg, isMe = msg.senderId == myId)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mensaje...", color = TextGray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NexusDarkGray,
                            unfocusedContainerColor = NexusDarkGray,
                            focusedTextColor = TextWhite,
                            cursorColor = NexusBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                scope.launch {
                                    val msg = Message(
                                        senderId = myId,
                                        text = messageText,
                                        // ✅ CORRECCIÓN: Usamos Clock en lugar de System
                                        timestamp = Clock.System.now().toEpochMilliseconds()
                                    )
                                    repo.sendMessage(chatId, msg)
                                    messageText = ""
                                    messages = repo.getMessages(chatId)
                                }
                            }
                        },
                        modifier = Modifier.background(NexusBlue, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMe) NexusBlue else NexusDarkGray)
                .padding(12.dp)
        ) {
            Text(message.text, color = TextWhite)
        }
    }
}