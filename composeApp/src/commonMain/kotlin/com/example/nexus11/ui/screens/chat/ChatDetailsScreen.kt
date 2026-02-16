package com.example.nexus11.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Mensaje
import com.example.nexus11.ui.screens.ImageViewerScreen
import com.example.nexus11.ui.screens.profile.rememberBase64ImageProfile
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        val haptics = LocalHapticFeedback.current
        val myId = authRepo.getCurrentUserId() ?: "anon"

        val themeColor = AppCache.themeColor
        val wallpaperBrush = remember(AppCache.wallpaperStyle) {
            when (AppCache.wallpaperStyle) {
                1 -> Brush.verticalGradient(listOf(Color(0xFF1A2980), Color(0xFF26D0CE)))
                2 -> Brush.linearGradient(listOf(Color(0xFF4e54c8), Color(0xFF8f94fb)))
                else -> Brush.linearGradient(listOf(Color.Black, Color.Black))
            }
        }

        var textState by remember { mutableStateOf("") }
        var isOtherTyping by remember { mutableStateOf(false) }
        var listaMensajes by remember { mutableStateOf(AppCache.messagesCache[chatId]?.reversed() ?: emptyList()) }
        val scrollState = rememberLazyListState()
        var replyToMessage by remember { mutableStateOf<Mensaje?>(null) }
        var editingMessage by remember { mutableStateOf<Mensaje?>(null) }

        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

        val imagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            resizeOptions = ResizeOptions(width = 800, height = 800, compressionQuality = 0.7),
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let { bytes ->
                    scope.launch {
                        val base64Str = withContext(Dispatchers.Default) {
                            "data:image/jpeg;base64," + bytes.encodeBase64().replace("\n", "").trim()
                        }
                        val nuevoMensaje = Mensaje(
                            senderId = myId, text = "",
                            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                            imageUrl = base64Str, isRead = false
                        )
                        repo.sendMessage(chatId, nuevoMensaje)
                        listaMensajes = listOf(nuevoMensaje) + listaMensajes
                        delay(100)
                        scrollState.animateScrollToItem(0)
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
        )

        LaunchedEffect(chatId) {
            repo.markMessagesAsRead(chatId, otherUserId)
            launch {
                while (true) {
                    try {
                        val nuevos = repo.getMessages(chatId)
                        val nuevosReversed = nuevos.reversed()
                        if (nuevosReversed != listaMensajes) {
                            listaMensajes = nuevosReversed
                            AppCache.messagesCache[chatId] = nuevos
                            if (nuevos.any { it.senderId == otherUserId && !it.isRead }) {
                                repo.markMessagesAsRead(chatId, otherUserId)
                            }
                        }
                        val typingMap = repo.getTypingStatus(chatId)
                        isOtherTyping = typingMap[otherUserId] == true
                    } catch (e: Exception) { }
                    delay(2000)
                }
            }
        }

        LaunchedEffect(textState) {
            repo.setTypingStatus(chatId, myId, textState.isNotEmpty())
        }

        Box(modifier = Modifier.fillMaxSize().background(wallpaperBrush)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    Surface(color = Color.Black.copy(alpha = 0.2f)) {
                        Column(modifier = Modifier.statusBarsPadding()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { navigator.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White) }
                                Column {
                                    Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    if (isOtherTyping) {
                                        Text("Escribiendo...", color = Color.White.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(0.1f))
                        }
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        AnimatedVisibility(visible = replyToMessage != null) {
                            Row(Modifier.fillMaxWidth().background(Color.Black.copy(0.3f)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Reply, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Respondiendo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(replyToMessage?.text ?: "Foto", maxLines = 1, color = Color.White.copy(0.7f), fontSize = 12.sp)
                                }
                                IconButton(onClick = { replyToMessage = null }) { Icon(Icons.Default.Close, null, tint = Color.White) }
                            }
                        }

                        AnimatedVisibility(visible = editingMessage != null) {
                            Row(Modifier.fillMaxWidth().background(themeColor.copy(0.2f)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Editando mensaje", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                IconButton(onClick = { editingMessage = null; textState = "" }) { Icon(Icons.Default.Close, null, tint = Color.White) }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editingMessage == null) {
                                IconButton(onClick = { imagePicker.launch() }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White)
                                }
                            }

                            OutlinedTextField(
                                value = textState,
                                onValueChange = { textState = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Escribe...", color = Color.White.copy(0.5f)) },
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(0.2f),
                                    unfocusedContainerColor = Color.Black.copy(0.2f),
                                    focusedBorderColor = Color.White.copy(0.3f),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val canSend = textState.isNotBlank()
                            IconButton(
                                onClick = {
                                    if (canSend) {
                                        scope.launch {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (editingMessage != null) {
                                                repo.updateMessage(chatId, editingMessage!!.id, text = textState, isEdited = true)
                                                listaMensajes = listaMensajes.map { if (it.id == editingMessage!!.id) it.copy(text = textState, isEdited = true) else it }
                                                editingMessage = null
                                            } else {
                                                val nuevoMensaje = Mensaje(
                                                    senderId = myId, text = textState,
                                                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                                    replyToId = replyToMessage?.id,
                                                    replyToText = replyToMessage?.text?.takeIf { it.isNotBlank() } ?: if(replyToMessage?.imageUrl != null) "Foto" else null,
                                                    isRead = false
                                                )
                                                repo.sendMessage(chatId, nuevoMensaje)
                                                listaMensajes = listOf(nuevoMensaje) + listaMensajes
                                                delay(50)
                                                scrollState.animateScrollToItem(0)
                                                replyToMessage = null
                                            }
                                            textState = ""
                                        }
                                    }
                                },
                                modifier = Modifier.size(48.dp).background(if(canSend) themeColor else Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    if (editingMessage != null) Icons.Default.Edit else Icons.AutoMirrored.Filled.Send,
                                    null,
                                    tint = if(canSend) Color.White else Color.White.copy(0.3f)
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    state = scrollState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    items(listaMensajes, key = { it.id }) { m ->
                        SwipeableMessageItem(
                            message = m,
                            isMe = m.senderId == myId,
                            textColor = Color.White,
                            surfaceColor = Color.Black.copy(0.3f),
                            myId = myId,
                            repo = repo,
                            chatId = chatId,
                            themeColor = themeColor,
                            onReply = { replyToMessage = it },
                            onReaction = { emoji ->
                                scope.launch {
                                    val newReactions = m.reactions.toMutableMap()
                                    if (newReactions[myId] == emoji) newReactions.remove(myId)
                                    else newReactions[myId] = emoji
                                    repo.updateMessage(chatId, m.id, reactions = newReactions)
                                    listaMensajes = listaMensajes.map { if (it.id == m.id) it.copy(reactions = newReactions) else it }
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onEdit = {
                                editingMessage = it
                                textState = it.text
                            },
                            onDelete = {
                                scope.launch {
                                    repo.deleteMessage(chatId, m.id)
                                    listaMensajes = listaMensajes.map {
                                        if (it.id == m.id) it.copy(text = "🚫 Mensaje eliminado", imageUrl = null)
                                        else it
                                    }
                                }
                            },
                            onImageClick = { url ->
                                navigator.push(ImageViewerScreen(url))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableMessageItem(
    message: Mensaje,
    isMe: Boolean,
    textColor: Color,
    surfaceColor: Color,
    myId: String,
    repo: DataRepository,
    chatId: String,
    themeColor: Color,
    onReply: (Mensaje) -> Unit,
    onReaction: (String) -> Unit,
    onEdit: (Mensaje) -> Unit,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onReply(message)
                return@rememberSwipeToDismissBoxState false
            }
            false
        }
    )

    var showMenu by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val clipboardManager = LocalClipboardManager.current
    val isDeleted = message.text == "🚫 Mensaje eliminado"

    SwipeToDismissBox(
        state = dismissState,
        // ✅ CORRECCIÓN: He quitado el icono de la flecha que se quedaba visible a la derecha
        backgroundContent = { Box(Modifier.fillMaxSize()) },
        enableDismissFromStartToEnd = false,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { if (!isDeleted) showMenu = true },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ),
                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                BubbleContent(message, isMe, textColor, surfaceColor, myId, onImageClick, themeColor)

                if (showMenu) {
                    Popup(
                        alignment = if (isMe) Alignment.TopEnd else Alignment.TopStart,
                        onDismissRequest = { showMenu = false },
                        offset = with(density) {
                            IntOffset(
                                x = (if(isMe) -20.dp else 20.dp).roundToPx(),
                                y = (-50).dp.roundToPx()
                            )
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color.Gray.copy(0.1f)),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("❤️", "😂", "😮", "👍").forEach { emoji ->
                                        Text(text = emoji, fontSize = 24.sp, modifier = Modifier.clickable { onReaction(emoji); showMenu = false })
                                    }
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Responder") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, null, Modifier.size(20.dp)) },
                                    onClick = { onReply(message); showMenu = false }
                                )
                                if (message.text.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Copiar") },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(20.dp)) },
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(message.text))
                                            showMenu = false
                                        }
                                    )
                                }
                                if (isMe) {
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(20.dp)) },
                                        onClick = { onEdit(message); showMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar", color = Color.Red) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = Color.Red) },
                                        onClick = { onDelete(); showMenu = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun BubbleContent(
    mensaje: Mensaje,
    esMio: Boolean,
    textColor: Color,
    surfaceColor: Color,
    myId: String,
    onImageClick: (String) -> Unit,
    themeColor: Color
) {
    val bubbleBrush = remember(themeColor) {
        if (esMio) {
            Brush.linearGradient(colors = listOf(themeColor, themeColor.copy(alpha = 0.7f)))
        } else {
            Brush.linearGradient(listOf(surfaceColor, surfaceColor))
        }
    }

    val contentColor = Color.White
    val isDeleted = mensaje.text == "🚫 Mensaje eliminado"

    val timeString = remember(mensaje.timestamp) {
        try {
            val instant = Instant.fromEpochMilliseconds(mensaje.timestamp)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
        } catch (e: Exception) { "" }
    }

    val msgImage = rememberBase64ImageProfile(mensaje.imageUrl)

    Column(
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
        modifier = Modifier.widthIn(max = 300.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 18.dp, topEnd = 18.dp,
                    bottomStart = if (esMio) 18.dp else 4.dp,
                    bottomEnd = if (esMio) 4.dp else 18.dp
                ))
                .background(brush = bubbleBrush, alpha = if (isDeleted) 0.5f else 1f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (mensaje.replyToId != null && !isDeleted) {
                    Row(Modifier.fillMaxWidth().background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                        Box(Modifier.width(3.dp).height(24.dp).background(Color.White.copy(0.7f)))
                        Spacer(Modifier.width(6.dp))
                        Text(mensaje.replyToText ?: "Mensaje", fontSize = 12.sp, maxLines = 1, color = Color.White.copy(0.9f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (msgImage != null && !isDeleted) {
                    Image(
                        bitmap = msgImage, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).clip(RoundedCornerShape(8.dp)).clickable { mensaje.imageUrl?.let { onImageClick(it) } },
                        contentScale = ContentScale.Crop
                    )
                    if (mensaje.text.isNotBlank()) Spacer(Modifier.height(8.dp))
                }
                if (mensaje.text.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(text = mensaje.text, color = contentColor, fontSize = 16.sp, fontStyle = if (isDeleted) FontStyle.Italic else FontStyle.Normal, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mensaje.isEdited && !isDeleted) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(10.dp), tint = contentColor.copy(0.7f))
                                Spacer(Modifier.width(2.dp))
                            }
                            Text(timeString, color = contentColor.copy(0.7f), fontSize = 10.sp)
                            if (esMio && !isDeleted) {
                                Spacer(Modifier.width(4.dp))
                                val icon = if (mensaje.isRead) Icons.Default.DoneAll else Icons.Default.Check
                                val tint = if (mensaje.isRead) Color.White.copy(0.95f) else contentColor.copy(0.7f)
                                Icon(icon, null, modifier = Modifier.size(14.dp), tint = tint)
                            }
                        }
                    }
                }
            }
        }
        if (mensaje.reactions.isNotEmpty() && !isDeleted) {
            Box(Modifier.offset(y = (-10).dp)) {
                Row(Modifier.shadow(2.dp, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    mensaje.reactions.values.distinct().take(3).forEach { emoji -> Text(emoji, fontSize = 11.sp) }
                    if (mensaje.reactions.size > 1) {
                        Text("${mensaje.reactions.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}