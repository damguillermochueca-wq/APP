package com.example.nexus11.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
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
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Mensaje
import com.example.nexus11.ui.screens.profile.rememberBase64ImageProfile
import com.example.nexus11.ui.theme.NexusBlue
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.Clock

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val rootNavigator = remember(navigator) {
            var nav = navigator
            while (nav.parent != null) {
                nav = nav.parent!!
            }
            nav
        }

        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }

        var chats by remember { mutableStateOf(AppCache.chatList) }
        var myId by remember { mutableStateOf("") }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        LaunchedEffect(Unit) {
            val user = authRepo.getCurrentUserId()
            if (user != null) myId = user

            while (true) {
                try {
                    if (myId.isNotEmpty()) {
                        val newChats = repo.getMyChats(myId)
                        if (newChats.size != chats.size || newChats.firstOrNull()?.second?.timestamp != chats.firstOrNull()?.second?.timestamp) {
                            chats = newChats
                            AppCache.chatList = newChats
                        }
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(3000)
            }
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
                // CABECERA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (navigator.canPop) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                        }
                    } else {
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "Mensajes",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (myId.isBlank() && chats.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NexusBlue)
                    }
                } else if (chats.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay conversaciones", color = textColor.copy(0.5f))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats, key = { it.first }) { (chatId, lastMsg) ->
                            val otherUserId = chatId.replace(myId, "").replace("_", "")

                            if (otherUserId.isNotEmpty()) {
                                ChatRowItem(
                                    otherUserId = otherUserId,
                                    lastMsg = lastMsg,
                                    myId = myId,
                                    repo = repo,
                                    textColor = textColor,
                                    onClick = { name ->
                                        // ⚡ CORRECCIÓN CRÍTICA: Actualizamos la Caché Global TAMBIÉN
                                        // Esto evita que el punto azul vuelva a aparecer al volver atrás
                                        val updatedChats = chats.map { (id, msg) ->
                                            if (id == chatId) id to msg.copy(isRead = true) else id to msg
                                        }

                                        chats = updatedChats       // Actualizamos UI
                                        AppCache.chatList = updatedChats // Actualizamos Caché (IMPORTANTE)

                                        // Navegamos
                                        rootNavigator.push(ChatDetailScreen(chatId, otherUserId, name))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRowItem(
    otherUserId: String,
    lastMsg: Mensaje,
    myId: String,
    repo: DataRepository,
    textColor: Color,
    onClick: (String) -> Unit
) {
    var otherUser by remember(otherUserId) { mutableStateOf(AppCache.users[otherUserId]) }

    LaunchedEffect(otherUserId) {
        if (otherUser == null) {
            val u = repo.getUser(otherUserId)
            if (u != null) {
                AppCache.users[otherUserId] = u
                otherUser = u
            }
        }
    }

    val displayName = otherUser?.username ?: "Cargando..."
    val avatarUrl = otherUser?.profileImageUrl
    val avatarBitmap = if (otherUser != null) rememberBase64ImageProfile(avatarUrl) else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(displayName) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AVATAR CON PUNTO VERDE
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    avatarBitmap != null -> Image(avatarBitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    !avatarUrl.isNullOrBlank() && !avatarUrl.startsWith("data:") -> KamelImage(asyncPainterResource(avatarUrl), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else -> Text(displayName.take(1).uppercase(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            otherUser?.let { user ->
                val now = Clock.System.now().toEpochMilliseconds()
                val diff = now - user.lastActive
                val isOnline = diff < 5 * 60 * 1000

                if (isOnline && user.showActivityStatus) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(displayName, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (lastMsg.senderId == myId) {
                    val icon = if (lastMsg.isRead) Icons.Default.DoneAll else Icons.Default.Check
                    val tint = if (lastMsg.isRead) Color(0xFF4FC3F7) else textColor.copy(0.6f)
                    Icon(icon, null, modifier = Modifier.size(16.dp), tint = tint)
                    Spacer(Modifier.width(4.dp))
                }

                val previewText = when {
                    lastMsg.text.isNotBlank() -> lastMsg.text
                    lastMsg.imageUrl != null -> "📷 Foto"
                    else -> "Mensaje"
                }

                val isUnread = !lastMsg.isRead && lastMsg.senderId != myId

                Text(
                    text = previewText,
                    color = if(isUnread) textColor else textColor.copy(0.6f),
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        if (!lastMsg.isRead && lastMsg.senderId != myId) {
            Box(Modifier.size(10.dp).background(NexusBlue, CircleShape))
        }
    }
}