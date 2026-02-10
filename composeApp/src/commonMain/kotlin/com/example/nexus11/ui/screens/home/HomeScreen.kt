package com.example.nexus11.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
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

// ✅ IMPORTS DE KAMEL (Para cargar imágenes de internet)
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

// ✅ IMPORTS DE DATOS
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.data.Story // Asegúrate de que este es el paquete correcto de tu Story
import com.example.nexus11.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateCreatePost: () -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    val dataRepo = remember { DataRepository() }

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        try {
            posts = dataRepo.getAllPosts()
            stories = dataRepo.getAllStories()
        } catch (e: Exception) {
            println("Error cargando Home: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(NexusBlack)) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NexusBlue)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // --- SECCIÓN HISTORIAS ---
                item {
                    Text(
                        "Historias",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { StoryItem("Tú", isAdd = true) }
                        items(stories) { story ->
                            StoryItem(name = story.username.ifBlank { "User" }, imageUrl = story.imageUrl)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // --- SECCIÓN POSTS (FEED) ---
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay publicaciones aún", color = TextGray)
                        }
                    }
                } else {
                    items(posts) { post ->
                        PostItem(post, onUserClick = { userId -> onNavigateToUser(userId) })
                        Spacer(Modifier.height(16.dp)) // Espacio entre posts
                    }
                }
            }
        }

        // Botón Flotante (FAB)
        FloatingActionButton(
            onClick = onNavigateCreatePost,
            containerColor = NexusBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) { Icon(Icons.Default.Add, "Nuevo Post") }
    }
}

// 📸 COMPONENTE DE HISTORIA
@Composable
fun StoryItem(name: String, imageUrl: String? = null, isAdd: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (isAdd) NexusDarkGray else NexusBlue) // Borde azul si es historia
                .padding(3.dp)
                .clip(CircleShape)
                .background(NexusBlack),
            contentAlignment = Alignment.Center
        ) {
            if (isAdd) {
                Icon(Icons.Default.Add, null, tint = NexusBlue)
            } else if (!imageUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { CircularProgressIndicator(strokeWidth = 2.dp, color = NexusBlue) }
                )
            } else {
                // Placeholder si no hay imagen
                Icon(Icons.Default.Image, null, tint = TextGray)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name.take(8), color = TextWhite, fontSize = 12.sp)
    }
}

// 📝 COMPONENTE DE POST
@Composable
fun PostItem(post: Post, onUserClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(NexusBlack)) {

        // 1. CABECERA (Avatar y Nombre)
        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable { onUserClick(post.userId) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NexusDarkGray),
                contentAlignment = Alignment.Center
            ) {
                // Intentamos cargar avatar, si no, mostramos inicial
                if (!post.userAvatarUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(post.userAvatarUrl),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = post.username.take(1).uppercase(),
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(post.username, color = TextWhite, fontWeight = FontWeight.Bold)
                Text("hace un momento", color = TextGray, fontSize = 12.sp)
            }
        }

        // 2. TEXTO DEL POST
        if (post.text.isNotEmpty()) {
            Text(
                text = post.text,
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 3. IMAGEN DEL POST (La parte importante) 📸
        if (!post.imageUrl.isNullOrBlank()) {
            KamelImage(
                resource = asyncPainterResource(post.imageUrl),
                contentDescription = "Imagen del post",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Altura fija para evitar saltos
                    .background(NexusDarkGray), // Fondo gris mientras carga
                contentScale = ContentScale.Crop,
                onLoading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NexusBlue)
                    }
                },
                onFailure = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.Red)
                            Text("Error al cargar", color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            )
        }

        // 4. ICONOS DE ACCIÓN (Like, Comentar)
        Row(Modifier.padding(16.dp)) {
            Icon(Icons.Default.FavoriteBorder, null, tint = TextWhite, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(20.dp))
            Icon(Icons.Default.ChatBubbleOutline, null, tint = TextWhite, modifier = Modifier.size(28.dp))
        }

        // Separador
        HorizontalDivider(color = NexusDarkGray, thickness = 0.5.dp)
    }
}