package com.example.nexus11.ui.screens.post

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.data.model.Post
import com.example.nexus11.ui.screens.HomeTab
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Pantalla de Creación de Publicaciones.
 * Gestiona la subida de contenido y la redirección al feed principal.
 */
class CreatePostScreen : Screen {
    @Composable
    override fun Content() {
        // Necesitamos el TabNavigator para poder volver al "Home" tras publicar
        val tabNavigator = LocalTabNavigator.current
        val scope = rememberCoroutineScope()

        val dataRepo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }

        var text by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

        val themeColor = AppCache.themeColor
        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        val singleImagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            resizeOptions = ResizeOptions(width = 1200, height = 1200, compressionQuality = 0.7),
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
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cancelar",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                // Al cancelar, limpiamos y volvemos al Home
                                text = ""
                                selectedImageBytes = null
                                selectedImageBitmap = null
                                tabNavigator.current = HomeTab
                            }
                        )

                        Button(
                            onClick = {
                                if (text.isNotBlank() || selectedImageBytes != null) {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val userId = authRepo.getCurrentUserId() ?: ""
                                            val currentUser = dataRepo.getUser(userId)
                                            val now = Clock.System.now().toEpochMilliseconds()

                                            val imageUrl = selectedImageBytes?.let { dataRepo.uploadImage(it) } ?: ""

                                            val newPost = Post(
                                                id = "post_$now",
                                                userId = userId,
                                                username = currentUser?.username ?: "Usuario",
                                                userAvatarUrl = currentUser?.profileImageUrl,
                                                description = text,
                                                imageUrl = imageUrl,
                                                timestamp = now
                                            )

                                            dataRepo.createPost(newPost)

                                            // ✅ CORRECCIÓN: Limpiamos estado y cambiamos de Pestaña.
                                            // navigator.pop() fallaba porque esta pantalla es la raíz de la pestaña.
                                            text = ""
                                            selectedImageBytes = null
                                            selectedImageBitmap = null
                                            isLoading = false

                                            // Redirigir al Feed
                                            tabNavigator.current = HomeTab

                                        } catch (e: Exception) {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && (text.isNotBlank() || selectedImageBytes != null),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Publicar", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = text,
                            onValueChange = { if (it.length <= 280) text = it },
                            placeholder = {
                                Text(
                                    "¿Qué hay de nuevo?",
                                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Light, color = textColor.copy(0.4f))
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = themeColor,
                                focusedTextColor = textColor
                            ),
                            textStyle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
                        )
                    }

                    item {
                        AnimatedVisibility(
                            visible = selectedImageBitmap != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 24.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(textColor.copy(0.05f))
                            ) {
                                selectedImageBitmap?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(32.dp)
                                        .clickable { selectedImageBitmap = null; selectedImageBytes = null },
                                    color = Color.Black.copy(0.7f),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = bgColor,
                    tonalElevation = 4.dp
                ) {
                    Column {
                        HorizontalDivider(color = textColor.copy(0.05f))
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { singleImagePicker.launch() }
                            ) {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(themeColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, "Imagen", tint = themeColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(if (selectedImageBitmap == null) "Añadir foto" else "Cambiar foto", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { text.length / 280f },
                                    modifier = Modifier.size(28.dp),
                                    color = if (text.length > 250) Color.Red else themeColor,
                                    strokeWidth = 3.dp,
                                    trackColor = textColor.copy(0.1f)
                                )
                                if (text.length > 200) {
                                    Text((280 - text.length).toString(), fontSize = 10.sp, color = if (text.length > 250) Color.Red else textColor.copy(0.6f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}