package com.example.nexus11.ui.screens.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.theme.*
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

// ✅ CLASE LIMPIA: Sin parámetros en el paréntesis ()
class CreatePostScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val dataRepo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }

        var text by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

        val singleImagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let {
                    selectedImageBytes = it
                    selectedImageBitmap = it.toImageBitmap()
                }
            }
        )

        Scaffold(
            containerColor = NexusBlack,
            topBar = {
                TopAppBar(
                    title = { Text("Crear Publicación", color = TextWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (text.isNotBlank() || selectedImageBytes != null) {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val userId = authRepo.getCurrentUserId() ?: "anon"
                                            val currentUser = dataRepo.getUser(userId)
                                            val realUsername = currentUser?.username ?: "Usuario"
                                            val realAvatar = currentUser?.profileImageUrl

                                            val now = Clock.System.now().toEpochMilliseconds()
                                            val newId = "post_$now"

                                            var finalImageUrl = ""
                                            if (selectedImageBytes != null) {
                                                // ✅ ARREGLO DE TIPO: Si devuelve null, usa cadena vacía
                                                finalImageUrl = dataRepo.uploadImage(selectedImageBytes!!) ?: ""
                                            }

                                            val newPost = Post(
                                                id = newId,
                                                userId = userId,
                                                username = realUsername,
                                                userAvatarUrl = realAvatar,
                                                description = text,
                                                imageUrl = finalImageUrl,
                                                timestamp = now,
                                                likes = 0
                                            )

                                            dataRepo.createPost(newPost)
                                            isLoading = false
                                            navigator.pop() // Volver al feed
                                        } catch (e: Exception) {
                                            println("❌ Error: ${e.message}")
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && (text.isNotBlank() || selectedImageBytes != null)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NexusBlue)
                            } else {
                                Icon(Icons.Default.Check, null, tint = NexusBlue)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBlack)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("¿Qué pasa?", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = NexusDarkGray,
                        unfocusedContainerColor = NexusDarkGray,
                        cursorColor = NexusBlue,
                        focusedBorderColor = NexusBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { singleImagePicker.launch() }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Añadir foto", tint = NexusBlue, modifier = Modifier.size(32.dp))
                    }
                    TextButton(onClick = { singleImagePicker.launch() }) {
                        Text("Añadir foto", color = NexusBlue)
                    }
                }

                if (selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(
                            bitmap = selectedImageBitmap!!,
                            contentDescription = "Imagen seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}