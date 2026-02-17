package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.User
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.decodeBase64Bytes
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FollowListScreen(
    val userId: String,
    val initialTab: Int = 0 // 0 = Seguidores, 1 = Siguiendo
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val themeColor = AppCache.themeColor

        var selectedTab by remember { mutableStateOf(initialTab) }
        val tabs = listOf("Seguidores", "Siguiendo")

        var followersList by remember { mutableStateOf<List<User>>(emptyList()) }
        var followingList by remember { mutableStateOf<List<User>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        // Carga eficiente de listas: Solo pide IDs y completa los usuarios desde la caché si existen.
        LaunchedEffect(userId) {
            try {
                val followerIds = repo.getFollowersIds(userId)
                val followingIds = repo.getFollowingIds(userId)

                if (AppCache.users.isEmpty()) {
                    val allUsers = repo.getAllUsers()
                    allUsers.forEach { AppCache.users[it.id] = it }
                }

                followersList = followerIds.mapNotNull { id ->
                    AppCache.users[id] ?: repo.getUser(id).also { if(it!=null) AppCache.users[id] = it }
                }
                followingList = followingIds.mapNotNull { id ->
                    AppCache.users[id] ?: repo.getUser(id).also { if(it!=null) AppCache.users[id] = it }
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                Column(
                    modifier = Modifier
                        .background(bgColor)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = themeColor)
                        }
                        Text("Red", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = bgColor,
                        contentColor = themeColor,
                        divider = { HorizontalDivider(color = textColor.copy(0.1f)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = themeColor)
                } else {
                    val listToShow = if (selectedTab == 0) followersList else followingList

                    if (listToShow.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if(selectedTab == 0) "Nadie por aquí aún" else "No sigue a nadie",
                                color = textColor.copy(0.5f)
                            )
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(listToShow) { user ->
                                UserRowItem(user, textColor) {
                                    // Pasamos isExternal = true para que la nueva pantalla sepa que viene de una pila
                                    // y ajuste su cabecera para no chocar con el reloj.
                                    navigator.push(ProfileScreen(user.id, isExternal = true))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... (UserRowItem y helper de imagen se mantienen igual que en el original)
@Composable
fun UserRowItem(user: User, textColor: Color, onClick: () -> Unit) {
    // Usamos el cargador con caché global
    val avatar = rememberBase64ImageProfileInList(user.profileImageUrl)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (avatar != null) {
                Image(bitmap = avatar, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (!user.profileImageUrl.isNullOrBlank() && !user.profileImageUrl.startsWith("data:")) {
                KamelImage(asyncPainterResource(user.profileImageUrl), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(user.username.take(1).uppercase(), fontWeight = FontWeight.Bold, color = textColor)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(user.username, fontWeight = FontWeight.Bold, color = textColor)
            if (user.profession.isNotEmpty()) {
                Text(user.profession, fontSize = 12.sp, color = textColor.copy(0.6f))
            }
        }
    }
}

// Helper optimizado con AppCache
@Composable
fun rememberBase64ImageProfileInList(base64String: String?): ImageBitmap? {
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