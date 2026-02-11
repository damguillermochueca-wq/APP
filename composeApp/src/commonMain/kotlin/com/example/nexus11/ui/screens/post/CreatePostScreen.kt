package com.example.nexus11.ui.screens.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.theme.NexusBlue
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreatePostScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        val dataRepo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }

        var text by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        fun clearAndPop() {
            text = ""
            selectedImageBytes = null
            selectedImageBitmap = null
            focusManager.clearFocus()
            navigator.pop()
        }

        val singleImagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            // ⚠️ ESTO ES LA CLAVE: Reduce la foto aquí para que tu DataRepository pueda subirla sin cambios
            resizeOptions = ResizeOptions(width = 1000, height = 1000, compressionQuality = 0.6),
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let {
                    selectedImageBytes = it
                    selectedImageBitmap = it.toImageBitmap()
                }
            }
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp)
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .imePadding() // Sube la pantalla con el teclado
            ) {

                // --- 1. BARRA SUPERIOR ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cancelar",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { clearAndPop() }
                        )

                        Button(
                            onClick = {
                                if (text.isNotBlank() || selectedImageBytes != null) {
                                    isLoading = true
                                    focusManager.clearFocus()
                                    scope.launch {
                                        try {
                                            val userId = authRepo.getCurrentUserId() ?: "anon"
                                            val currentUser = dataRepo.getUser(userId)
                                            val now = Clock.System.now().toEpochMilliseconds()

                                            var finalImageUrl = ""
                                            if (selectedImageBytes != null) {
                                                finalImageUrl = dataRepo.uploadImage(selectedImageBytes!!) ?: ""
                                            }

                                            val newPost = Post(
                                                id = "post_$now",
                                                userId = userId,
                                                username = currentUser?.username ?: "Usuario",
                                                userAvatarUrl = currentUser?.profileImageUrl,
                                                description = text,
                                                imageUrl = finalImageUrl,
                                                timestamp = now
                                            )

                                            dataRepo.createPost(newPost)

                                            isLoading = false
                                            clearAndPop()

                                        } catch (e: Exception) {
                                            isLoading = false
                                            println("Error: ${e.message}")
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && (text.isNotBlank() || selectedImageBytes != null),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusBlue,
                                disabledContainerColor = NexusBlue.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Publicar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }

                // --- 2. CONTENIDO ---
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        // ✅ CORREGIDO: Usamos verticalAlignment en lugar de crossAxisAlignment
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Yo", color = textColor, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = {
                                    Text("¿Qué está pasando?", color = textColor.copy(alpha = 0.5f), fontSize = 18.sp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = NexusBlue,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                textStyle = TextStyle(fontSize = 18.sp, lineHeight = 24.sp)
                            )
                        }
                    }

                    if (selectedImageBitmap != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.Gray.copy(0.2f), RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    bitmap = selectedImageBitmap!!,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                IconButton(
                                    onClick = {
                                        selectedImageBitmap = null
                                        selectedImageBytes = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(Color.Black.copy(0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // --- 3. BARRA INFERIOR ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                ) {
                    HorizontalDivider(color = textColor.copy(0.1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { singleImagePicker.launch() }) {
                            Icon(Icons.Default.Image, contentDescription = "Foto", tint = NexusBlue)
                        }
                        Text(
                            text = "Añadir imagen",
                            color = NexusBlue,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { singleImagePicker.launch() }
                        )
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}