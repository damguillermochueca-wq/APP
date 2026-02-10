package com.example.nexus11.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

class ChatListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // Obtenemos el navegador "padre" (fuera de las pestañas) para poder ir al detalle
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val dataRepo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val myId = authRepo.getCurrentUserId() ?: ""

        var users by remember { mutableStateOf<List<User>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            try {
                val allUsers = dataRepo.getAllUsers()
                users = allUsers.filter { it.id != myId }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }

        val filteredUsers = users.filter { it.username.contains(searchQuery, ignoreCase = true) }

        Scaffold(
            containerColor = NexusBlack,
            topBar = {
                // ❌ SIN BOTÓN ATRÁS (Es pestaña raíz)
                TopAppBar(
                    title = { Text("Chats", fontWeight = FontWeight.Bold, color = TextWhite) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(NexusBlack)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = NexusBlue) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexusDarkGray,
                        unfocusedContainerColor = NexusDarkGray,
                        focusedBorderColor = NexusBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        cursorColor = NexusBlue
                    ),
                    singleLine = true
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NexusBlue)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredUsers) { user ->
                            val chatId = if (myId < user.id) "${myId}_${user.id}" else "${user.id}_${myId}"
                            ChatItem(user) {
                                // Navegamos fuera de las pestañas para ocultar la barra inferior
                                navigator.push(ChatDetailScreen(chatId, user.id, user.username))
                            }
                            HorizontalDivider(modifier = Modifier.padding(start = 88.dp), color = NexusDarkGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(NexusDarkGray), contentAlignment = Alignment.Center) {
            if (!user.profileImageUrl.isNullOrBlank()) {
                KamelImage(resource = asyncPainterResource(user.profileImageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(user.username.take(1).uppercase(), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(user.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Toca para chatear", color = TextGray, fontSize = 13.sp)
        }
    }
}