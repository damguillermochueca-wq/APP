package com.example.nexus11.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onBack: () -> Unit,
    onChatClick: (String, String, String) -> Unit
) {
    val dataRepo = remember { DataRepository() }
    val authRepo = remember { AuthRepository() }
    val myId = authRepo.getCurrentUserId() ?: ""

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        try {
            val allUsers = dataRepo.getAllUsers()
            users = allUsers.filter { it.id != myId }
        } catch (e: Exception) {
            println("Error cargando usuarios: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val filteredUsers = users.filter {
        it.username.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = NexusBlack,
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = NexusBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NexusBlack)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar personas...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = NexusBlue) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NexusDarkGray,
                    unfocusedContainerColor = NexusDarkGray,
                    focusedBorderColor = NexusBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
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
                        ChatItem(user) {
                            // Generamos un ID único para el chat (siempre en orden alfabético/numérico)
                            val chatId = if (myId < user.id) "${myId}_${user.id}" else "${user.id}_${myId}"
                            onChatClick(chatId, user.id, user.username)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 88.dp),
                            color = NexusDarkGray,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(NexusDarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (!user.profileImageUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(user.profileImageUrl),
                    contentDescription = "Avatar de ${user.username}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { CircularProgressIndicator(modifier = Modifier.scale(0.5f), color = NexusBlue) },
                    onFailure = {
                        // Si falla la imagen, mostramos la inicial
                        Text(user.username.take(1).uppercase(), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                )
            } else {
                Text(
                    text = user.username.take(1).uppercase(),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user.username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toca para chatear",
                color = TextGray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}