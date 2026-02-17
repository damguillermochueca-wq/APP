package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import kotlinx.coroutines.launch

class AppearanceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        val myId = authRepo.getCurrentUserId() ?: ""

        val colors = listOf(
            Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFF44336),
            Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0),
            Color(0xFF00BCD4), Color(0xFF607D8B), Color(0xFFFFC107),
            Color(0xFF795548), Color(0xFF000000)
        )

        // Estado local para la previsualización antes de guardar
        var selectedColor by remember { mutableStateOf(AppCache.themeColor) }
        var selectedWallpaper by remember { mutableStateOf(AppCache.wallpaperStyle) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        fun applyChanges() {
            scope.launch {
                val colorInt = selectedColor.toArgb()
                val colorLong = colorInt.toLong()

                // SERIALIZACIÓN SEGURA (Hex String):
                // Evita problemas de signo en Integers entre plataformas.
                val hexString = (colorLong and 0xFFFFFFFFL).toString(16).uppercase()

                // 1. Actualización inmediata en RAM y Disco
                AppCache.themeColor = selectedColor
                AppCache.wallpaperStyle = selectedWallpaper
                AppCache.settings.putString("local_theme_color_v5", hexString)
                AppCache.settings.putInt("local_wallpaper_id_v5", selectedWallpaper)

                // 2. Sincronización con Nube
                repo.updateUserAppearance(myId, colorLong, selectedWallpaper)

                navigator.pop()
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
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = selectedColor)
                            }
                            Text("Apariencia", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        TextButton(onClick = { applyChanges() }) {
                            Text("Guardar", color = selectedColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = textColor.copy(0.1f))
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(bgColor)
            ) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("COLOR DE ACENTO", selectedColor)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colors) { color ->
                        ColorCircle(color, selectedColor) { selectedColor = color }
                    }
                }
                Spacer(Modifier.height(32.dp))
                SectionTitle("FONDO DE CHAT", selectedColor)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WallpaperOption("Oscuro", 0, selectedWallpaper, selectedColor) { selectedWallpaper = 0 }
                    WallpaperOption("Océano", 1, selectedWallpaper, selectedColor) { selectedWallpaper = 1 }
                    WallpaperOption("Galaxia", 2, selectedWallpaper, selectedColor) { selectedWallpaper = 2 }
                }
                Spacer(Modifier.height(40.dp))

                // Componentes "Fake" para previsualizar el cambio de tema sin salir
                SectionTitle("VISTA PREVIA: CHAT", selectedColor)
                FakeChatPreview(selectedColor, selectedWallpaper)
                Spacer(Modifier.height(32.dp))
                SectionTitle("VISTA PREVIA: PERFIL", selectedColor)
                FakeProfilePreview(selectedColor, textColor)
                Spacer(Modifier.height(50.dp))
            }
        }
    }

    // --- COMPONENTES DE PREVISUALIZACIÓN ---
    // (Simulan la UI real pero son estáticos para mostrar los colores)
    @Composable
    fun FakeChatPreview(themeColor: Color, wallpaperId: Int) {
        val wallpaperBrush = when (wallpaperId) {
            1 -> Brush.verticalGradient(listOf(Color(0xFF1A2980), Color(0xFF26D0CE)))
            2 -> Brush.linearGradient(listOf(Color(0xFF4e54c8), Color(0xFF8f94fb)))
            else -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(wallpaperBrush).padding(20.dp)) {
            Text("Hoy", fontSize = 11.sp, color = Color.White.copy(0.6f), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)).background(Color.Black.copy(0.3f)).padding(12.dp)) { Text("¿Qué tal queda el fondo?", fontSize = 15.sp, color = Color.White) }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.align(Alignment.End).clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)).background(Brush.linearGradient(listOf(themeColor, themeColor.copy(0.7f)))).padding(12.dp)) {
                Row(verticalAlignment = Alignment.Bottom) { Text("¡Me encanta! 😍", fontSize = 15.sp, color = Color.White); Spacer(Modifier.width(6.dp)); Icon(Icons.Default.DoneAll, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(14.dp)) }
            }
        }
    }

    @Composable
    fun FakeProfilePreview(themeColor: Color, textColor: Color) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(60.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)), contentAlignment = Alignment.Center) { Text("U", color = textColor.copy(0.5f), fontWeight = FontWeight.Bold, fontSize = 24.sp) }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(themeColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(38.dp).weight(1f)) { Text("Seguir", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = {}, border = BorderStroke(1.dp, textColor.copy(0.1f)), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(38.dp).weight(1f)) { Text("Mensaje", fontSize = 13.sp, color = textColor) }
            }
        }
    }

    @Composable fun SectionTitle(text: String, color: Color) { Text(text, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)) }
    @Composable fun ColorCircle(color: Color, selectedColor: Color, onClick: () -> Unit) { val isSelected = color.toArgb() == selectedColor.toArgb(); Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(color).clickable(onClick = onClick).border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.onBackground, CircleShape), contentAlignment = Alignment.Center) { if (isSelected) Icon(Icons.Default.Check, null, tint = if (color == Color.Black) Color.White else Color.Black) } }
    @Composable fun RowScope.WallpaperOption(label: String, id: Int, currentId: Int, themeColor: Color, onClick: () -> Unit) { val isSelected = id == currentId; Surface(modifier = Modifier.weight(1f).height(48.dp).clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = if (isSelected) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(if (isSelected) 2.dp else 0.dp, themeColor)) { Box(contentAlignment = Alignment.Center) { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) } } }
}