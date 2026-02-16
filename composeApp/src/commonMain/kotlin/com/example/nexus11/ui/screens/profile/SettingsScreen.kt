package com.example.nexus11.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.nexus11.data.model.User
import com.example.nexus11.ui.screens.auth.LoginScreen
import kotlinx.coroutines.launch

data class SettingsScreen(val initialUser: User) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo = remember { DataRepository() }
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        // Leemos el color directamente de AppCache (reactivo)
        val themeColor = AppCache.themeColor

        var allowNotif by remember { mutableStateOf(initialUser.allowNotifications) }
        var showActivity by remember { mutableStateOf(initialUser.showActivityStatus) }
        var biometricEnabled by remember { mutableStateOf(initialUser.biometricEnabled) }
        var dataSaver by remember { mutableStateOf(false) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        fun saveSettings() {
            scope.launch {
                val colorInt = AppCache.themeColor.toArgb()
                val colorLong = colorInt.toLong()

                // 🔥 GUARDADO BLINDADO V5 🔥
                // Convertimos a UInt para evitar negativos y aseguramos 8 caracteres (ej: FF2196F3)
                val hexString = colorInt.toUInt().toString(16).uppercase().padStart(8, '0')

                // Guardamos en local inmediatamente
                AppCache.settings.putString("local_theme_color_v5", hexString)

                repo.updateUserSettings(
                    userId = initialUser.id,
                    allowNotif = allowNotif,
                    showActivity = showActivity,
                    biometricEnabled = biometricEnabled,
                    themeColorHex = colorLong
                )

                val latestUser = AppCache.users[initialUser.id] ?: initialUser
                AppCache.users[initialUser.id] = latestUser.copy(
                    allowNotifications = allowNotif,
                    showActivityStatus = showActivity,
                    biometricEnabled = biometricEnabled,
                    themeColorHex = colorLong
                )
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            // 1. Quitamos los insets automáticos del sistema
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                Column(
                    modifier = Modifier
                        .background(bgColor)
                        // 2. ✅ AÑADIMOS ESTO: Baja el contenido lo justo para respetar la barra de estado
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = themeColor)
                        }
                        Text("Ajustes", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                SettingsSectionTitle("Personalización", themeColor)
                SettingsClickableItem("Apariencia", "Color de acento y fondo de chat", Icons.Default.Brush, textColor, themeColor) { navigator.push(AppearanceScreen()) }

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                SettingsSectionTitle("Privacidad y Seguridad", themeColor)
                SettingsSwitchItem("Estado de Actividad", "Mostrar cuando estás en línea", Icons.Default.Visibility, showActivity, { showActivity = it; saveSettings() }, textColor, themeColor)
                SettingsSwitchItem("Bloqueo Biométrico", "FaceID o Huella al entrar", Icons.Default.Fingerprint, biometricEnabled, { biometricEnabled = it; saveSettings() }, textColor, themeColor)
                SettingsClickableItem("Privacidad de Cuenta", "Quién puede ver tus posts", Icons.Default.Lock, textColor, themeColor) { }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                SettingsSectionTitle("Notificaciones", themeColor)
                SettingsSwitchItem("Notificaciones Push", "Alertas de mensajes y likes", Icons.Default.Notifications, allowNotif, { allowNotif = it; saveSettings() }, textColor, themeColor)
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                SettingsSectionTitle("Almacenamiento", themeColor)
                SettingsSwitchItem("Ahorro de Datos", "Reducir calidad de imágenes", Icons.Default.DataUsage, dataSaver, { dataSaver = it }, textColor, themeColor)
                SettingsClickableItem("Uso de Red", "Ver consumo total de megas", Icons.Default.Storage, textColor, themeColor) { }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                SettingsSectionTitle("Más información", themeColor)
                SettingsClickableItem("Idioma", "Español (España)", Icons.Default.Language, textColor, themeColor) { }
                SettingsClickableItem("Ayuda", "Centro de soporte Nexus 11", Icons.AutoMirrored.Filled.Help, textColor, themeColor) { }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        scope.launch {
                            authRepo.logout()
                            AppCache.users.clear()
                            AppCache.chatList = emptyList()
                            navigator.replaceAll(LoginScreen())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(40.dp))
                Text("Nexus 11 v2.0.4\nHecho con ❤️", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = textColor.copy(0.3f), fontSize = 11.sp)
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---
@Composable
fun SettingsSectionTitle(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        color = color,
        fontWeight = FontWeight.Black,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String, subtitle: String, icon: ImageVector,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    textColor: Color, themeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = themeColor.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
            Text(subtitle, fontSize = 12.sp, color = textColor.copy(0.5f))
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColor,
                uncheckedThumbColor = Color.Gray.copy(0.5f),
                uncheckedTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String, subtitle: String, icon: ImageVector,
    textColor: Color, themeColor: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = themeColor.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
            Text(subtitle, fontSize = 12.sp, color = textColor.copy(0.5f))
        }
        Icon(Icons.Default.ChevronRight, null, tint = textColor.copy(0.2f))
    }
}