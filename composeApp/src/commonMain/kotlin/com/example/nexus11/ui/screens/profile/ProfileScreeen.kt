package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.ProfileOptions
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.screens.chat.ChatDetailScreen
import com.example.nexus11.ui.screens.home.PostCard
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileScreen(val userId: String, val isExternal: Boolean = false) : Screen {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val rootNavigator = navigator.parent?.parent ?: navigator.parent ?: navigator

        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        val myId = remember { authRepo.getCurrentUserId() ?: "" }
        val isMe = myId == userId
        val themeColor = AppCache.themeColor

        var user by remember { mutableStateOf(AppCache.users[userId]) }
        var userPosts by remember { mutableStateOf(AppCache.posts.filter { it.userId == userId }) }
        var isFollowing by remember { mutableStateOf(false) }
        var followersCount by remember { mutableStateOf(0) }
        var followingCount by remember { mutableStateOf(0) }
        var isLoading by remember { mutableStateOf(user == null) }
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Fotos", "Textos")

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        LaunchedEffect(userId) {
            if (user == null) {
                user = repo.getUser(userId)
                if (user != null) AppCache.users[userId] = user!!
            }
            if (AppCache.posts.isEmpty()) {
                val all = repo.getAllPosts()
                AppCache.posts.clear()
                AppCache.posts.addAll(all)
            }
            userPosts = AppCache.posts.filter { it.userId == userId }
            val stats = repo.getFollowStats(userId)
            followersCount = stats.first
            followingCount = stats.second
            if (!isMe) isFollowing = repo.amIFollowing(myId, userId)
            isLoading = false
        }

        val avatarPicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            resizeOptions = ResizeOptions(width = 500, height = 500, compressionQuality = 0.6),
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let { bytes ->
                    scope.launch {
                        val base64Str = withContext(Dispatchers.Default) { bytes.encodeBase64().replace("\n", "").trim() }
                        val finalUrl = "data:image/jpeg;base64,$base64Str"
                        val currentUser = user ?: repo.getUser(userId)
                        if (currentUser != null) {
                            val optimisticUser = currentUser.copy(profileImageUrl = finalUrl)
                            AppCache.users[userId] = optimisticUser
                            user = optimisticUser
                            AppCache.bitmapCache.remove(finalUrl)
                        }
                        repo.updateUserAvatar(userId, bytes)
                    }
                }
            }
        )

        val postsToShow = remember(userPosts, selectedTabIndex) {
            if (selectedTabIndex == 0) userPosts.filter { !it.imageUrl.isNullOrBlank() }
            else userPosts.filter { it.imageUrl.isNullOrBlank() }
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
                // CABECERA
                Column(
                    modifier = Modifier
                        .background(bgColor)
                        .then(if (isExternal) Modifier.statusBarsPadding() else Modifier)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isMe) {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = themeColor)
                                }
                            } else {
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isMe) "Mi Perfil" else user?.username ?: "Perfil",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        if (isMe) {
                            Row {
                                IconButton(onClick = { rootNavigator.push(SearchUserScreen()) }) {
                                    Icon(Icons.Default.Search, null, tint = themeColor)
                                }
                                if (user != null) {
                                    IconButton(onClick = { rootNavigator.push(SettingsScreen(user!!)) }) {
                                        Icon(Icons.Default.Settings, null, tint = themeColor)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeColor)
                    }
                } else if (user != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                // FOTO
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)).clickable(enabled = isMe) { avatarPicker.launch() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val avatar = user?.profileImageUrl
                                        val profileBitmap = rememberBase64ImageProfile(avatar)
                                        if (!avatar.isNullOrBlank()) {
                                            if (profileBitmap != null) Image(profileBitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            else if (!avatar.startsWith("data:image")) KamelImage(asyncPainterResource(avatar!!), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Text(user!!.username.take(1).uppercase(), fontSize = 40.sp, color = textColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (isMe) {
                                        Box(Modifier.size(30.dp).clip(CircleShape).background(themeColor).border(2.dp, bgColor, CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp)); Text(text = "@${user!!.username}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)

                                if (user!!.profession.isNotEmpty() || user!!.hobby.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                        if (user!!.profession.isNotEmpty()) TagChip(user!!.profession, themeColor)
                                        Spacer(Modifier.width(4.dp))
                                        if (user!!.hobby.isNotEmpty()) TagChip(user!!.hobby, Color(0xFFE91E63)) // Rosa para hobby
                                    }
                                }

                                // 🟢 AÑADIDO: VIBE (STATUS)
                                if (user!!.status.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp)) // Un poco de espacio extra
                                    TagChip(user!!.status, Color(0xFF4CAF50)) // Verde para status
                                }
                                if (user!!.bio.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp)); Text(user!!.bio, fontSize = 14.sp, color = textColor.copy(0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                                }
                                Spacer(Modifier.height(20.dp))

                                // ESTADÍSTICAS CLICKABLES
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    StatItem("${userPosts.size}", "Posts", textColor, Modifier.weight(1f))

                                    StatItem(
                                        value = "$followersCount",
                                        label = "Seguidores",
                                        textColor = textColor,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            // Abrimos lista de seguidores (Tab 0)
                                            rootNavigator.push(FollowListScreen(userId, initialTab = 0))
                                        }
                                    )

                                    StatItem(
                                        value = "$followingCount",
                                        label = "Siguiendo",
                                        textColor = textColor,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            // Abrimos lista de seguidos (Tab 1)
                                            rootNavigator.push(FollowListScreen(userId, initialTab = 1))
                                        }
                                    )
                                }
                                Spacer(Modifier.height(24.dp))

                                if (!isMe) {
                                    Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    if (isFollowing) { repo.unfollowUser(myId, userId); isFollowing = false; followersCount--; AppCache.myFollowingIds.remove(userId) }
                                                    else { repo.followUser(myId, userId); isFollowing = true; followersCount++; AppCache.myFollowingIds.add(userId) }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else themeColor, contentColor = if (isFollowing) textColor else Color.White),
                                            shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Icon(if(isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (isFollowing) "Siguiendo" else "Seguir", fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { val chatId = repo.getChatId(myId, userId); rootNavigator.push(ChatDetailScreen(chatId, userId, user!!.username)) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = textColor),
                                            shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Default.Message, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Mensaje", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    var showEditDialog by remember { mutableStateOf(false) }
                                    Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth(0.9f).height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = themeColor), shape = RoundedCornerShape(12.dp)) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Editar Perfil", fontWeight = FontWeight.Bold)
                                    }
                                    if (showEditDialog) {
                                        EditProfileDialog(user = user!!, onDismiss = { showEditDialog = false }, onSave = { prof, hob, stat, bio -> scope.launch { repo.updateProfileInfo(userId, prof, hob, stat, bio); val updated = user!!.copy(profession = prof, hobby = hob, status = stat, bio = bio); user = updated; AppCache.users[userId] = updated; showEditDialog = false } })
                                    }
                                }
                            }
                        }
                        stickyHeader {
                            TabRow(selectedTabIndex = selectedTabIndex, containerColor = bgColor, contentColor = themeColor) {
                                tabs.forEachIndexed { index, title -> Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = FontWeight.Bold) }) }
                            }
                        }
                        items(items = postsToShow, key = { it.id }) { post ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                PostCard(post = post, repo = repo, contentColor = textColor, cardBgColor = MaterialTheme.colorScheme.surfaceVariant, themeColor = themeColor, onUserClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------
// COMPONENTES AUXILIARES
// ---------------------------------------------

@Composable
fun StatItem(
    value: String,
    label: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textColor)
        Text(text = label, fontSize = 13.sp, color = textColor.copy(0.6f), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
fun TagChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(user: User, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var bio by remember { mutableStateOf(user.bio) }
    var profession by remember { mutableStateOf(user.profession) }
    var hobby by remember { mutableStateOf(user.hobby) }
    var status by remember { mutableStateOf(user.status) }
    var expProf by remember { mutableStateOf(false) }
    var expHobby by remember { mutableStateOf(false) }
    var expStatus by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biografía") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                ExposedDropdown(
                    "Oficio / Estudios",
                    ProfileOptions.professions,
                    profession,
                    expProf,
                    { expProf = it },
                    { profession = it; expProf = false }
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdown(
                    "Hobby",
                    ProfileOptions.hobbies,
                    hobby,
                    expHobby,
                    { expHobby = it },
                    { hobby = it; expHobby = false }
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdown(
                    "Estado (Vibe)",
                    ProfileOptions.statuses,
                    status,
                    expStatus,
                    { expStatus = it },
                    { status = it; expStatus = false }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(profession, hobby, status, bio) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdown(
    label: String,
    options: List<String>,
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option) })
            }
        }
    }
}

@Composable
fun rememberBase64ImageProfile(base64String: String?): ImageBitmap? {
    if (base64String.isNullOrBlank() || !base64String.startsWith("data:image")) return null
    // Cache Check
    if (AppCache.bitmapCache.containsKey(base64String)) {
        return AppCache.bitmapCache[base64String]
    }

    var bitmap by remember(base64String) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(base64String) {
        withContext(Dispatchers.Default) {
            try {
                // Decodificado y guardado en caché
                val decoded = base64String.substringAfter(",").decodeBase64Bytes().toImageBitmap()
                AppCache.bitmapCache[base64String] = decoded
                withContext(Dispatchers.Main) {
                    bitmap = decoded
                }
            } catch (e: Exception) {
                // Fallo silencioso si la imagen está corrupta
            }
        }
    }
    return bitmap
}