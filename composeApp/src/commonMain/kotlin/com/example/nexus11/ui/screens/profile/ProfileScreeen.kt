package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

data class ProfileScreen(val userId: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }

        var user by remember { mutableStateOf<User?>(null) }
        var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        // Cargar datos del usuario y sus posts
        LaunchedEffect(userId) {
            user = repo.getUser(userId)
            val allPosts = repo.getAllPosts()
            // Filtramos solo los posts de este usuario
            userPosts = allPosts.filter { it.userId == userId }
            isLoading = false
        }

        Scaffold(
            containerColor = NexusBlack,
            topBar = {
                TopAppBar(
                    title = { Text(user?.username ?: "Perfil", color = TextWhite) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = NexusBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NexusBlue)
                }
            } else {
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    // --- CABECERA DEL PERFIL ---
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Grande
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(NexusDarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user?.profileImageUrl.isNullOrBlank()) {
                                KamelImage(
                                    resource = asyncPainterResource(user!!.profileImageUrl!!),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = user?.username?.take(1)?.uppercase() ?: "?",
                                    fontSize = 40.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "@${user?.username}",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${userPosts.size} publicaciones",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }

                    Divider(color = NexusDarkGray)

                    // --- GRID DE FOTOS ---
                    if (userPosts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aún no hay publicaciones", color = TextGray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(userPosts) { post ->
                                if (post.imageUrl.isNotEmpty()) {
                                    KamelImage(
                                        resource = asyncPainterResource(post.imageUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .aspectRatio(1f) // Cuadrado perfecto
                                            .background(NexusDarkGray)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}