package com.example.nexus11.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.screens.profile.ProfileScreen
import com.example.nexus11.ui.theme.NexusBlue
import com.preat.peekaboo.image.picker.toImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        // Obtenemos el navigator parent para que el perfil tape la barra inferior al navegar
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }

        var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var displayedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        val listState = rememberLazyListState()
        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        // ✅ Lógica de Paginación (Precarga de 7 en 7)
        val pageSize = 7
        var currentPage by remember { mutableStateOf(1) }

        LaunchedEffect(Unit) {
            allPosts = repo.getAllPosts()
            displayedPosts = allPosts.take(pageSize)
            isLoading = false
        }

        // Detectar scroll para cargar más
        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                if (layoutInfo.totalItemsCount == 0) false
                else {
                    val lastVisibleItem = visibleItemsInfo.last()
                    lastVisibleItem.index + 1 == layoutInfo.totalItemsCount
                }
            }
        }

        LaunchedEffect(isAtBottom) {
            if (isAtBottom && displayedPosts.size < allPosts.size) {
                currentPage++
                displayedPosts = allPosts.take(currentPage * pageSize)
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
                // Cabecera compacta
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "NEXUS 11",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NexusBlue)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayedPosts) { post ->
                            PostCard(
                                post = post,
                                repo = repo,
                                contentColor = textColor,
                                cardBgColor = MaterialTheme.colorScheme.surfaceVariant,
                                onUserClick = { userId ->
                                    // Navegamos al perfil
                                    navigator.push(ProfileScreen(userId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    repo: DataRepository,
    contentColor: Color,
    cardBgColor: Color,
    onUserClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var likes by remember { mutableStateOf(post.likes) }
    var commentText by remember { mutableStateOf("") }
    var commentsList by remember { mutableStateOf(post.comments.entries.sortedBy { it.key }.map { it.value }) }

    // ✅ Lógica para que la foto de perfil se actualice siempre
    var currentAvatarUrl by remember { mutableStateOf(post.userAvatarUrl) }

    LaunchedEffect(post.userId) {
        val user = repo.getUser(post.userId)
        if (user?.profileImageUrl != null) {
            currentAvatarUrl = user.profileImageUrl
        }
    }

    // Lógica híbrida de imágenes del post (tu fix que ya funciona)
    val postBitmap = remember(post.imageUrl) {
        try {
            if (post.imageUrl?.startsWith("data:image") == true) {
                val base64String = post.imageUrl.substringAfter(",")
                base64String.decodeBase64Bytes().toImageBitmap()
            } else null
        } catch (e: Exception) { null }
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, NexusBlue), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onUserClick(post.userId) }
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(NexusBlue), contentAlignment = Alignment.Center) {
                    if (!currentAvatarUrl.isNullOrBlank()) {
                        val avatarUrl = if (currentAvatarUrl!!.startsWith("data:image")) currentAvatarUrl!! else "data:image/jpeg;base64,${currentAvatarUrl}"

                        if (avatarUrl.contains("base64,")) {
                            val bitmap = remember(avatarUrl) {
                                try { avatarUrl.substringAfter(",").decodeBase64Bytes().toImageBitmap() } catch(e:Exception){null}
                            }
                            if (bitmap != null) Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            KamelImage(asyncPainterResource(avatarUrl), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    } else {
                        Text(post.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(post.username, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (post.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(post.description, color = contentColor, fontSize = 15.sp)
            }

            if (postBitmap != null) {
                Spacer(Modifier.height(12.dp))
                Image(bitmap = postBitmap, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            } else if (!post.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                KamelImage(resource = asyncPainterResource(post.imageUrl), contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { repo.likePost(post); likes++ } }) {
                    Icon(if (likes > post.likes) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (likes > post.likes) Color.Red else contentColor)
                }
                Text("$likes", color = contentColor)
            }
            if (commentsList.isNotEmpty()) {
                Column { commentsList.takeLast(3).forEach { Text(it, color = contentColor.copy(0.7f), fontSize = 13.sp) } }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText, onValueChange = { commentText = it },
                    placeholder = { Text("Comentar...", color = contentColor.copy(0.5f)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NexusBlue, unfocusedBorderColor = Color.Transparent)
                )
                IconButton(onClick = { if (commentText.isNotBlank()) { scope.launch { repo.commentPost(post, commentText, "Yo"); commentsList = commentsList + "Yo: $commentText"; commentText = "" } } }) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = NexusBlue)
                }
            }
        }
    }
}