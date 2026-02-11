package com.example.nexus11.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.screens.profile.ProfileScreen
import com.example.nexus11.ui.theme.NexusBlue
import com.example.nexus11.ui.theme.TextGray
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val repository = remember { DataRepository() }
        val scope = rememberCoroutineScope()

        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }

        fun refreshPosts() {
            scope.launch {
                isRefreshing = true
                delay(1000)
                try {
                    posts = repository.getAllPosts()
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                } finally {
                    isRefreshing = false
                }
            }
        }

        LaunchedEffect(Unit) { refreshPosts(); isLoading = false }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("Nexus 11 Feed", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isLoading && posts.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NexusBlue)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(posts) { post ->
                            PostItemCard(post, { scope.launch { repository.likePost(post); refreshPosts() } }, { text -> scope.launch { repository.commentPost(post, text, "Usuario"); refreshPosts() } }, { userId -> navigator.push(ProfileScreen(userId)) })
                        }
                    }
                }
                if (isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), color = NexusBlue)
            }
        }
    }
}

@Composable
fun PostItemCard(post: Post, onLikeClick: () -> Unit, onCommentSend: (String) -> Unit, onUserClick: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    val contentColor = MaterialTheme.colorScheme.onSurface

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = contentColor),
        border = BorderStroke(1.dp, NexusBlue.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onUserClick(post.userId) }.padding(12.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    if (!post.userAvatarUrl.isNullOrBlank()) KamelImage(asyncPainterResource(post.userAvatarUrl), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Text(post.username.take(1).uppercase(), fontWeight = FontWeight.Bold, color = contentColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(post.username, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (post.imageUrl.isNotEmpty()) KamelImage(asyncPainterResource(post.imageUrl), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).heightIn(max = 350.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant))

            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) { Icon(if (post.likes > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Like", tint = if (post.likes > 0) MaterialTheme.colorScheme.error else contentColor, modifier = Modifier.size(24.dp)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${post.likes} Me gusta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (post.description.isNotEmpty()) Text(post.description, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))

                // ✅ LÓGICA DE COMENTARIOS SEGURA
                if (post.comments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (post.comments.size > 1) Text("Ver los ${post.comments.size} comentarios", fontSize = 13.sp, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))

                    val lastEntry = post.comments.entries.sortedBy { it.key }.last()
                    val parts = lastEntry.value.split(": ", limit = 2)
                    val user = if (parts.size > 1) parts[0] else "Anónimo"
                    val msg = if (parts.size > 1) parts[1] else parts[0]

                    Row(verticalAlignment = Alignment.Top) {
                        Text("$user ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = contentColor)
                        Text(msg, fontSize = 13.sp, color = contentColor.copy(alpha = 0.9f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = commentText, onValueChange = { commentText = it }, placeholder = { Text("Añade un comentario...", fontSize = 14.sp, color = TextGray) }, modifier = Modifier.weight(1f), singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = NexusBlue, focusedTextColor = contentColor, unfocusedTextColor = contentColor), textStyle = LocalTextStyle.current.copy(fontSize = 14.sp))
                    if (commentText.isNotBlank()) TextButton(onClick = { onCommentSend(commentText); commentText = "" }) { Text("Publicar", color = NexusBlue, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}