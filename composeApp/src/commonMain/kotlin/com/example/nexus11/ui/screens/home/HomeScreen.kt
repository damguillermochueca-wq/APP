package com.example.nexus11.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
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
import com.example.nexus11.ui.screens.post.CreatePostScreen
import com.example.nexus11.ui.screens.profile.ProfileScreen
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // Obtenemos el navegador padre para salir de las pestañas al navegar
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val repository = remember { DataRepository() }
        val scope = rememberCoroutineScope()

        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        // Cargar posts al iniciar
        LaunchedEffect(Unit) {
            posts = repository.getAllPosts()
            isLoading = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nexus 11 Feed") }
                    // ❌ Sin actions (el chat está abajo)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(CreatePostScreen()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Post")
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts) { post ->
                            PostCard(
                                post = post,
                                onLikeClick = {
                                    scope.launch {
                                        repository.likePost(post)
                                        // Refrescamos la lista para ver el like al instante
                                        posts = repository.getAllPosts()
                                    }
                                },
                                onCommentSend = { text ->
                                    scope.launch {
                                        repository.commentPost(post, text, "Yo")
                                        posts = repository.getAllPosts()
                                    }
                                },
                                onUserClick = { userId ->
                                    // ✅ Navegamos al perfil del usuario
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
    onLikeClick: () -> Unit,
    onCommentSend: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // --- CABECERA: AVATAR Y NOMBRE (Clickable) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(post.userId) } // <--- Click aquí lleva al perfil
                    .padding(bottom = 8.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (!post.userAvatarUrl.isNullOrBlank()) {
                        KamelImage(
                            resource = asyncPainterResource(post.userAvatarUrl),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Inicial si no hay foto
                        Text(
                            text = post.username.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nombre
                Text(
                    text = "@${post.username}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // --- IMAGEN DEL POST ---
            if (post.imageUrl.isNotEmpty()) {
                KamelImage(
                    resource = asyncPainterResource(post.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Altura fija para que se vea bien
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTONES DE ACCIÓN (LIKE) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.likes > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.likes > 0) Color.Red else Color.Gray
                    )
                }
                Text("${post.likes} likes", fontWeight = FontWeight.Bold)
            }

            // --- DESCRIPCIÓN ---
            if (post.description.isNotEmpty()) {
                Text(
                    text = post.description,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- LISTA DE COMENTARIOS (Preview) ---
            if (post.comments.isNotEmpty()) {
                Text("Comentarios:", fontSize = 12.sp, color = Color.Gray)
                // Mostramos máximo 3 comentarios para no saturar
                post.comments.values.take(3).forEach { com ->
                    Text(com, fontSize = 13.sp, modifier = Modifier.padding(vertical = 1.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CAJA PARA ESCRIBIR COMENTARIO ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Escribe un comentario...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onCommentSend(commentText)
                            commentText = "" // Limpiar campo
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.Blue)
                }
            }
        }
    }
}