package com.example.nexus11.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.screens.profile.ProfileScreen
import com.preat.peekaboo.image.picker.toImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val myId = authRepo.getCurrentUserId() ?: ""
        val scope = rememberCoroutineScope()

        // 🎨 LEEMOS EL COLOR DEL TEMA (Reactivo)
        val themeColor = AppCache.themeColor

        // DATOS
        var allPosts by remember { mutableStateOf(AppCache.posts) }
        var followingIds by remember { mutableStateOf(AppCache.myFollowingIds.toSet()) }

        // ESTADOS DE UI
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Descubrir", "Siguiendo", "Para ti")
        var initialLoading by remember { mutableStateOf(AppCache.posts.isEmpty()) }

        // ESTADO PULL TO REFRESH
        var isRefreshing by remember { mutableStateOf(false) }
        val pullState = rememberPullToRefreshState()

        fun loadData() {
            scope.launch {
                isRefreshing = true
                try {
                    delay(500)
                    val newPosts = repo.getAllPosts()
                    if (newPosts.isNotEmpty()) {
                        AppCache.posts.clear()
                        AppCache.posts.addAll(newPosts)
                        allPosts.clear()
                        allPosts.addAll(newPosts)
                    }
                    if (myId.isNotEmpty()) {
                        val remoteFollowing = repo.getMyFollowing(myId)
                        AppCache.myFollowingIds.clear()
                        AppCache.myFollowingIds.addAll(remoteFollowing)
                        followingIds = remoteFollowing
                    }
                } catch (e: Exception) {
                } finally {
                    isRefreshing = false
                }
            }
        }

        if (pullState.isRefreshing) {
            LaunchedEffect(true) { if (!isRefreshing) loadData() }
        }
        LaunchedEffect(isRefreshing) { if (!isRefreshing) pullState.endRefresh() }
        LaunchedEffect(Unit) {
            if (initialLoading) { loadData(); initialLoading = false }
        }

        val displayedPosts = remember(allPosts, selectedTabIndex, followingIds) {
            val myUser = AppCache.users[myId]
            when (selectedTabIndex) {
                0 -> allPosts
                1 -> allPosts.filter { followingIds.contains(it.userId) }
                2 -> if (myUser == null) emptyList() else allPosts.filter { post ->
                    val author = AppCache.users[post.userId]
                    if (author != null && author.id != myId) {
                        (author.profession.isNotEmpty() && author.profession == myUser.profession) ||
                                (author.hobby.isNotEmpty() && author.hobby == myUser.hobby)
                    } else false
                }
                else -> emptyList()
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0.dp)
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).background(MaterialTheme.colorScheme.background)) {

                // CABECERA
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    Box(modifier = Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                        Text("NEXUS 11", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = themeColor, // 🎨 Color de las pestañas
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size)
                                TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = themeColor) // 🎨 Línea debajo
                        },
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if(selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if(selectedTabIndex == index) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // 🎨 Texto coloreado
                                    )
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().nestedScroll(pullState.nestedScrollConnection)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (displayedPosts.isEmpty() && !initialLoading && !isRefreshing) {
                            item {
                                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay publicaciones", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                }
                            }
                        } else {
                            items(items = displayedPosts, key = { it.id }) { post ->
                                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    PostCard(
                                        post = post,
                                        repo = repo,
                                        contentColor = MaterialTheme.colorScheme.onBackground,
                                        cardBgColor = MaterialTheme.colorScheme.surfaceVariant,
                                        themeColor = themeColor, // 🎨 Pasamos el color
                                        onUserClick = { navigator.push(ProfileScreen(it)) }
                                    )
                                }
                            }
                        }
                    }

                    if (pullState.progress > 0 || isRefreshing) {
                        PullToRefreshContainer(
                            state = pullState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = themeColor // 🎨 Rueda de carga con color
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------
// HELPERS (PostCard y Carga de Imágenes)
// -----------------------------------------------------------

@Composable
fun rememberBase64Image(base64String: String?): ImageBitmap? {
    if (base64String.isNullOrBlank() || !base64String.startsWith("data:image")) return null
    if (AppCache.bitmapCache.containsKey(base64String)) return AppCache.bitmapCache[base64String]
    var bitmap by remember(base64String) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(base64String) {
        withContext(Dispatchers.Default) {
            try {
                val decoded = base64String.substringAfter(",").decodeBase64Bytes().toImageBitmap()
                AppCache.bitmapCache[base64String] = decoded
                withContext(Dispatchers.Main) { bitmap = decoded }
            } catch (e: Exception) { }
        }
    }
    return bitmap
}

@Composable
fun PostCard(post: Post, repo: DataRepository, contentColor: Color, cardBgColor: Color, themeColor: Color, onUserClick: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }
    val myId = authRepo.getCurrentUserId() ?: ""
    val myUsername = AppCache.users[myId]?.username ?: "Yo"
    var likedByMe by remember { mutableStateOf(post.likedBy.containsKey(myId)) }
    var likesCount by remember { mutableStateOf(post.likes + post.likedBy.size) }
    var commentText by remember { mutableStateOf("") }
    var localComments by remember { mutableStateOf(post.comments.values.toList()) }
    var currentAvatarUrl by remember { mutableStateOf(AppCache.users[post.userId]?.profileImageUrl ?: post.userAvatarUrl) }

    LaunchedEffect(Unit) {
        if (AppCache.users[post.userId] == null) {
            val user = repo.getUser(post.userId)
            if (user != null) {
                AppCache.users[post.userId] = user
                currentAvatarUrl = user.profileImageUrl
            }
        }
    }

    val avatarBitmap = rememberBase64Image(currentAvatarUrl)
    val postBitmap = rememberBase64Image(post.imageUrl)

    Card(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, themeColor.copy(alpha = 0.5f)), RoundedCornerShape(20.dp)), // 🎨 Borde sutil del color tema
        colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onUserClick(post.userId) }) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) { // 🎨 Fondo Avatar
                    if (avatarBitmap != null) Image(avatarBitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else if (!currentAvatarUrl.isNullOrBlank() && !currentAvatarUrl!!.startsWith("data:image")) KamelImage(asyncPainterResource(currentAvatarUrl!!), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Text(post.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
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
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    scope.launch {
                        val wasLiked = likedByMe
                        likedByMe = !wasLiked
                        likesCount += if(wasLiked) -1 else 1
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        repo.toggleLikePost(post.id, myId, wasLiked)
                    }
                }) {
                    Icon(if (likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (likedByMe) Color.Red else contentColor)
                }
                Text("$likesCount", color = contentColor)
            }

            if (localComments.isNotEmpty()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    localComments.takeLast(3).forEach { Text(it, fontSize = 13.sp, color = contentColor.copy(0.8f)) }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText, onValueChange = { commentText = it },
                    placeholder = { Text("Comentar...", color = contentColor.copy(0.5f)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor, // 🎨 Borde al escribir
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (commentText.isNotBlank()) {
                        scope.launch {
                            val fullComment = "$myUsername: $commentText"
                            repo.commentPost(post, commentText, myUsername)
                            localComments = localComments + fullComment
                            commentText = ""
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = themeColor) // 🎨 Icono enviar
                }
            }
        }
    }
}