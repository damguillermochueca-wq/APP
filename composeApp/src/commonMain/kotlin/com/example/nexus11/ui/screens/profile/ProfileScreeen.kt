package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
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
import com.example.nexus11.data.model.Post
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.screens.home.PostCard
import com.example.nexus11.ui.theme.NexusBlue
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.*
import kotlinx.coroutines.launch

data class ProfileScreen(val userId: String) : Screen {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        var user by remember { mutableStateOf<User?>(null) }
        var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Fotos", "Textos")

        val myId = remember { authRepo.getCurrentUserId() }
        val isMe = myId == userId

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        // Selector para cambiar la foto de perfil (con compresión para que cargue)
        val avatarPicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            resizeOptions = ResizeOptions(width = 500, height = 500, compressionQuality = 0.6),
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let { bytes ->
                    scope.launch {
                        repo.updateUserAvatar(userId, bytes)
                        user = repo.getUser(userId) // Refrescar info
                    }
                }
            }
        )

        LaunchedEffect(userId) {
            isLoading = true
            user = repo.getUser(userId)
            userPosts = repo.getAllPosts().filter { it.userId == userId }
            isLoading = false
        }

        val postsToShow = remember(userPosts, selectedTabIndex) {
            if (selectedTabIndex == 0) {
                userPosts.filter { !it.imageUrl.isNullOrBlank() }
            } else {
                userPosts.filter { it.imageUrl.isNullOrBlank() }
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
                // --- CABECERA AJUSTADA ---
                // Agregamos statusBarsPadding para que no se pegue al Notch en iOS/Android
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                    }
                    Text("Perfil", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NexusBlue)
                    }
                } else if (user != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Info del Usuario
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray.copy(0.2f))
                                            .clickable(enabled = isMe) { avatarPicker.launch() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val avatar = user?.profileImageUrl
                                        if (!avatar.isNullOrBlank()) {
                                            if (avatar.startsWith("data:image")) {
                                                val bitmap = remember(avatar) {
                                                    try { avatar.substringAfter(",").decodeBase64Bytes().toImageBitmap() }
                                                    catch (e: Exception) { null }
                                                }
                                                if (bitmap != null) Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            } else {
                                                KamelImage(asyncPainterResource(avatar), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            }
                                        } else {
                                            Text(user!!.username.take(1).uppercase(), fontSize = 40.sp, color = textColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (isMe) {
                                        Box(
                                            modifier = Modifier.size(30.dp).clip(CircleShape).background(NexusBlue).border(2.dp, bgColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(user!!.username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text("${userPosts.size} Publicaciones", color = textColor.copy(0.6f), fontSize = 14.sp)
                            }
                        }

                        // Pestañas (Sticky)
                        stickyHeader {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = bgColor,
                                contentColor = NexusBlue,
                                divider = { HorizontalDivider(color = textColor.copy(0.1f)) }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { selectedTabIndex = index },
                                        text = { Text(title, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }

                        // Lista de Posts (reutiliza el PostCard que ya carga las fotos bien)
                        items(postsToShow) { post ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                PostCard(
                                    post = post,
                                    repo = repo,
                                    contentColor = textColor,
                                    cardBgColor = MaterialTheme.colorScheme.surfaceVariant,
                                    onUserClick = { /* Ya estamos en su perfil */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}