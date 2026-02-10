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
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.launch
import com.example.nexus11.getCurrentTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dataRepo = remember { DataRepository() }
    val authRepo = remember { AuthRepository() }

    var text by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Variables para la imagen
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                selectedImageBytes = it // Guardamos los bytes para subir
                selectedImageBitmap = it.toImageBitmap() // Guardamos la imagen para verla
            }
        }
    )

    Scaffold(
        containerColor = NexusBlack,
        topBar = {
            TopAppBar(
                title = { Text("Crear Publicación", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NexusBlue)
                    }
                },
                actions = {
                    // 🚀 BOTÓN PUBLICAR CON LÓGICA DE SUBIDA
                    IconButton(
                        onClick = {
                            if (text.isNotBlank() || selectedImageBytes != null) {
                                isLoading = true
                                scope.launch {
                                    val userId = authRepo.getCurrentUserId() ?: "anon"
                                    val currentUser = dataRepo.getUser(userId)
                                    val realUsername = currentUser?.username ?: "Usuario"
                                    val realAvatar = currentUser?.profileImageUrl

                                    val now = getCurrentTimeMillis()
                                    val newId = "post_$now"

                                    // 1️⃣ SUBIMOS LA IMAGEN (Si hay)
                                    var finalImageUrl: String? = null
                                    if (selectedImageBytes != null) {
                                        println("📤 Subiendo imagen a ImgBB...")
                                        finalImageUrl = dataRepo.uploadImage(selectedImageBytes!!)
                                        println("🔗 URL recibida: $finalImageUrl")
                                    }

                                    // 2️⃣ CREAMOS EL POST (Con la URL si existe)
                                    val newPost = Post(
                                        id = newId,
                                        userId = userId,
                                        username = realUsername,
                                        userAvatarUrl = realAvatar,
                                        text = text,
                                        imageUrl = finalImageUrl, // ✅ AQUÍ VA LA URL DE LA NUBE
                                        timestamp = now
                                    )

                                    dataRepo.createPost(newPost)

                                    isLoading = false
                                    onPostCreated()
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
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
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
                    cursorColor = NexusBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { singleImagePicker.launch() }) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = NexusBlue, modifier = Modifier.size(32.dp))
                }
                Text("Añadir foto", color = NexusBlue)
            }
            if (selectedImageBitmap != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                    Image(
                        bitmap = selectedImageBitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}