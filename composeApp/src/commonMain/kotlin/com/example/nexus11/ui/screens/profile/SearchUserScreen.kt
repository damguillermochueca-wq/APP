package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.User
import com.preat.peekaboo.image.picker.toImageBitmap
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de Búsqueda de Usuarios.
 * Utiliza filtrado local para respuesta inmediata al teclear.
 */
class SearchUserScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val themeColor = AppCache.themeColor
        val focusManager = LocalFocusManager.current
        val scope = rememberCoroutineScope()

        val myId = remember { authRepo.getCurrentUserId() ?: "" }

        var searchQuery by remember { mutableStateOf("") }
        // Cargamos todos los usuarios al entrar para filtrar rápidamente en RAM
        var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        LaunchedEffect(Unit) {
            scope.launch {
                val users = repo.getAllUsers()
                // Filtramos para no mostrarnos a nosotros mismos en la búsqueda
                allUsers = users.filter { it.id != myId }
                isLoading = false
            }
        }

        // Lógica de filtrado reactiva
        val filteredUsers = remember(searchQuery, allUsers) {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                allUsers.filter {
                    it.username.contains(searchQuery, ignoreCase = true) ||
                            it.bio.contains(searchQuery, ignoreCase = true)
                }
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
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, start = 4.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = themeColor)
                        }

                        // Campo de búsqueda con diseño personalizado
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .border(1.dp, themeColor.copy(0.2f), RoundedCornerShape(26.dp)),
                            placeholder = { Text("Buscar usuarios...", color = textColor.copy(0.5f), fontSize = 15.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = themeColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = themeColor) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, null, tint = textColor.copy(0.5f))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(bgColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = themeColor)
                } else {
                    if (filteredUsers.isEmpty() && searchQuery.isNotEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, null, tint = textColor.copy(0.1f), modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No se encontraron usuarios", color = textColor.copy(0.4f), fontWeight = FontWeight.Medium)
                        }
                    } else if (searchQuery.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Empieza a escribir para buscar", color = textColor.copy(0.3f), fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { Spacer(Modifier.height(8.dp)) }

                            items(filteredUsers) { user ->
                                UserResultItem(user, themeColor, textColor) {
                                    // Abrimos perfil en modo externo (con cabecera ajustada)
                                    navigator.push(ProfileScreen(user.id, isExternal = true))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun UserResultItem(user: User, themeColor: Color, textColor: Color, onClick: () -> Unit) {
        val profileBitmap = rememberBase64ImageProfileInSearch(user.profileImageUrl)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = user.username.take(1).uppercase(),
                        color = themeColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = textColor
                )
                val subtext = if (user.bio.isNotEmpty()) user.bio else user.profession
                if (subtext.isNotEmpty()) {
                    Text(
                        text = subtext,
                        maxLines = 1,
                        fontSize = 14.sp,
                        color = textColor.copy(0.5f)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 88.dp, end = 16.dp), color = textColor.copy(0.05f))
    }

    @Composable
    fun rememberBase64ImageProfileInSearch(base64String: String?): ImageBitmap? {
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
}