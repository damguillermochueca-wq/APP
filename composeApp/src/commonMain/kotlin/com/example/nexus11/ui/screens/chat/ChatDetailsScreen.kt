package com.example.nexus11.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 👇 TUS IMPORTS (Asegúrate de que estos paquetes existan en tu proyecto)
import com.example.nexus11.getCurrentTimeMillis
import com.example.nexus11.data.ChatRepository // Usamos ChatRepository en lugar de DataRepository
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.Message // Asegúrate de que este modelo esté definido
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    otherUserId: String,
    userName: String,
    onBack: () -> Unit
) {
    // Usamos los repositorios correctos
    val chatRepo = remember { ChatRepository() }
    val authRepo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val myId = authRepo.getCurrentUserId() ?: "anon"

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()

    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var replyingTo by remember { mutableStateOf<Message?>(null) }
    // Simulación de "escribiendo" (esto requeriría implementación real en backend)
    var isOtherUserTyping by remember { mutableStateOf(false) }

    // Carga inicial y polling de mensajes
    LaunchedEffect(chatId) {
        while (true) {
            try {
                val fetchedMessages = chatRepo.getMessages(chatId)
                if (fetchedMessages != messages) {
                    messages = fetchedMessages
                    // Scroll al final si hay mensajes nuevos
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
                // Aquí podrías añadir lógica real para verificar si el otro usuario escribe
                // isOtherUserTyping = chatRepo.checkOtherUserTyping(chatId, otherUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(2000) // Polling cada 2 segundos
        }
    }

    Scaffold(
        containerColor = NexusBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NexusDarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = userName,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isOtherUserTyping) {
                                Text(
                                    text = "escribiendo...",
                                    color = NexusBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "En línea",
                                    color = NexusBlue.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = NexusBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // LISTA DE MENSAJES
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(messages) { msg ->
                    SwipeToReplyContainer(
                        isMe = msg.senderId == myId,
                        onReplyTriggered = { replyingTo = msg }
                    ) {
                        MessageBubble(
                            message = msg,
                            isMe = msg.senderId == myId,
                            onReaction = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isOtherUserTyping) {
                    item {
                        TypingIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // BARRA DE RESPUESTA / EDICIÓN
            AnimatedVisibility(
                visible = replyingTo != null || editingMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = Color(0xFF111111),
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            tint = NexusBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingMessage != null) "Editando" else "Respondiendo a",
                                color = NexusBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = (editingMessage?.text ?: replyingTo?.text ?: "").take(40) + "...",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = {
                            editingMessage = null
                            replyingTo = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = TextGray
                            )
                        }
                    }
                }
            }

            // INPUT DE TEXTO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusBlack)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            "Mensaje...",
                            color = TextGray.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexusDarkGray,
                        unfocusedContainerColor = NexusDarkGray,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = NexusBlue
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            scope.launch {
                                // Enviar mensaje
                                chatRepo.sendMessage(chatId, myId, messageText)

                                // Limpiar estado
                                messageText = ""
                                replyingTo = null

                                // Recargar mensajes para ver el nuevo
                                messages = chatRepo.getMessages(chatId)
                                listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (messageText.isBlank()) NexusDarkGray else NexusBlue,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (messageText.isBlank()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (messageText.isBlank()) TextGray else Color.White
                    )
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun SwipeToReplyContainer(
    isMe: Boolean,
    onReplyTriggered: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Solo permitimos swipe si NO es mi mensaje (opcional, o para ambos)
    // Aquí lo dejo para ambos como en tu código original

    Box(contentAlignment = Alignment.CenterStart) {
        // Icono de respuesta que aparece al deslizar
        if (offsetX.value > 10f) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = null,
                tint = NexusBlue,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(24.dp)
                    .scale((offsetX.value / 100f).coerceIn(0.5f, 1f))
                    .alpha((offsetX.value / 100f).coerceIn(0f, 1f))
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            // Solo permitimos arrastrar hacia la derecha (valores positivos)
                            val newValue = offsetX.value + delta * 0.4f
                            if (newValue >= 0) {
                                offsetX.snapTo(newValue)
                            }
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX.value > 80f) { // Umbral para activar
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onReplyTriggered()
                        }
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onReaction: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            // Mostrar referencia si es una respuesta (esto requeriría que tu modelo Message tenga este campo)
            /* if (message.replyToText != null) {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp).alpha(0.7f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(2.dp).height(20.dp).background(NexusBlue, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Respuesta...", color = TextGray, fontSize = 11.sp)
                }
            }
            */

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .background(if (isMe) NexusBlue else NexusDarkGray)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = TextWhite,
                        fontSize = 15.sp
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hora simulada (deberías formatear message.timestamp)
                        Text(
                            text = "12:30",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")

    @Composable
    fun animateDot(delay: Int): State<Float> {
        return transition.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot"
        )
    }

    val o1 by animateDot(0)
    val o2 by animateDot(200)
    val o3 by animateDot(400)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(NexusDarkGray)
            .padding(16.dp, 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Dot(o1)
            Spacer(Modifier.width(4.dp))
            Dot(o2)
            Spacer(Modifier.width(4.dp))
            Dot(o3)
        }
    }
}

@Composable
fun Dot(y: Float) {
    Box(
        modifier = Modifier
            .offset(y = y.dp)
            .size(8.dp)
            .background(NexusBlue, CircleShape)
    )
}