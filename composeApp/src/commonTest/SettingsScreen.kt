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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

        // 🎨 LEEMOS EL COLOR ACTUAL DEL CACHÉ (Reactivo)
        val themeColor = AppCache.themeColor

        // Estados locales sincronizados con el usuario inicial
        var allowNotif by remember { mutableStateOf(initialUser.allowNotifications) }
        var showActivity by remember { mutableStateOf(initialUser.showActivityStatus) }
        var biometricEnabled by remember { mutableStateOf(initialUser.biometricEnabled) }
        var dataSaver by remember { mutableStateOf(false) }

        val bgColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground

        // ✅ FUNCIÓN PARA GUARDAR CAMBIOS DE FORMA PERSISTENTE
        fun saveSettings() {
            scope.launch {
                // 1. Guardar en Firebase
                repo.updateUserSettings(initialUser.id, allowNotif, showActivity, biometricEnabled)
                
                // 2. Actualizar la caché local para que el resto de la app se entere
                val updatedUser = initialUser.copy(
                    allowNotifications = allowNotif,
                    showActivityStatus = showActivity,
                    biometricEnabled = biometricEnabled
                )
                AppCache.users[initialUser.id] = updatedUser
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                Column(Modifier.background(bgColor).statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
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
                // --- SECCIÓN 1: PERSONALIZACIÓN ---
                SettingsSectionTitle("Personalización", themeColor)

                SettingsClickableItem(
                    title = "Apariencia",
                    subtitle = "Color de acento y fondo de chat",
                    icon = Icons.Default.Brush,
                    textColor = textColor,
                    themeColor = themeColor, // Pasamos el color para el icono
                    onClick = { navigator.push(AppearanceScreen()) }
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                // --- SECCIÓN 2: PRIVACIDAD Y SEGURIDAD ---
                SettingsSectionTitle("Privacidad y Seguridad", themeColor)

                SettingsSwitchItem(
                    title = "Estado de Actividad",
                    subtitle = "Mostrar cuando estás en línea",
                    icon = Icons.Default.Visibility,
                    checked = showActivity,
                    themeColor = themeColor,
                    onCheckedChange = { 
                        showActivity = it
                        saveSettings() // Persistencia inmediata
                    },
                    textColor = textColor
                )

                SettingsSwitchItem(
                    title = "Bloqueo Biométrico",
                    subtitle = "FaceID o Huella al entrar",
                    icon = Icons.Default.Fingerprint,
                    checked = biometricEnabled,
                    themeColor = themeColor,
                    onCheckedChange = { 
                        biometricEnabled = it
                        saveSettings() // Persistencia inmediata
                    },
                    textColor = textColor
                )

                SettingsClickableItem(
                    title = "Privacidad de Cuenta",
                    subtitle = "Quién puede ver tus posts",
                    icon = Icons.Default.Lock,
                    textColor = textColor,
                    themeColor = themeColor,
                    onClick = { /* Implementar más adelante */ }
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                // --- SECCIÓN 3: NOTIFICACIONES ---
                SettingsSectionTitle("Notificaciones", themeColor)

                SettingsSwitchItem(
                    title = "Notificaciones Push",
                    subtitle = "Alertas de mensajes y likes",
                    icon = Icons.Default.Notifications,
                    checked = allowNotif,
                    themeColor = themeColor,
                    onCheckedChange = { 
                        allowNotif = it
                        saveSettings() // Persistencia inmediata
                    },
                    textColor = textColor
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                // --- SECCIÓN 4: DATOS Y ALMACENAMIENTO ---
                SettingsSectionTitle("Datos y Almacenamiento", themeColor)

                SettingsSwitchItem(
                    title = "Ahorro de Datos",
                    subtitle = "Reducir calidad de imágenes",
                    icon = Icons.Default.DataUsage,
                    checked = dataSaver,
                    themeColor = themeColor,
                    onCheckedChange = { dataSaver = it },
                    textColor = textColor
                )

                SettingsClickableItem(
                    title = "Uso de Red",
                    subtitle = "Ver consumo total de datos",
                    icon = Icons.Default.Storage,
                    textColor = textColor,
                    themeColor = themeColor,
                    onClick = { }
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = textColor.copy(0.05f))

                // --- SECCIÓN 5: AYUDA E INFORMACIÓN ---
                SettingsSectionTitle("Más información", themeColor)

                SettingsClickableItem(
                    title = "Idioma",
                    subtitle = "Español (España)",
                    icon = Icons.Default.Language,
                    textColor = textColor,
                    themeColor = themeColor,
                    onClick = { }
                )

                SettingsClickableItem(
                    title = "Ayuda",
                    subtitle = "Centro de soporte Nexus 11",
                    icon = Icons.AutoMirrored.Filled.Help,
                    textColor = textColor,
                    themeColor = themeColor,
                    onClick = { }
                )

                Spacer(Modifier.height(32.dp))

                // --- BOTÓN CERRAR SESIÓN ---
                Button(
                    onClick = {
                        scope.launch {
                            authRepo.logout()
                            AppCache.users.clear()
                            AppCache.chatList = emptyList()
                            navigator.replaceAll(LoginScreen())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(40.dp))
                Text(
                    "Nexus 11 v2.0.4\nHecho con ❤️ para ti",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = textColor.copy(0.3f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---

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