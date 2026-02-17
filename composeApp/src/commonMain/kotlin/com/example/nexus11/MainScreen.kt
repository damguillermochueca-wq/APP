package com.example.nexus11.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.nexus11.data.AppCache
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.data.DataRepository
import com.example.nexus11.ui.screens.chat.ChatListScreen
import com.example.nexus11.ui.screens.home.HomeScreen
import com.example.nexus11.ui.screens.post.CreatePostScreen
import com.example.nexus11.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.delay

/**
 * Contenedor Principal de la Aplicación.
 * Gestiona la navegación de nivel superior mediante pestañas (Tabs).
 * Mantiene el ciclo de vida de la sesión activa (Heartbeat).
 */
class MainScreen : Screen {
    @Composable
    override fun Content() {
        val authRepo = remember { AuthRepository() }
        val repo = remember { DataRepository() }
        val myId = authRepo.getCurrentUserId()

        // 🎨 TEMA REACTIVO: La barra de navegación escucha los cambios de color
        // en tiempo real desde la configuración global (AppCache).
        val themeColor = AppCache.themeColor

        // Inicialización de sesión y sistema de presencia
        LaunchedEffect(myId) {
            if (myId != null) {
                // 1. Precarga de datos del usuario actual para la UI
                try {
                    val me = repo.getUser(myId)
                    if (me != null) {
                        AppCache.users[myId] = me
                        // NOTA TÉCNICA: No sobrescribimos themeColor desde la DB aquí
                        // para respetar la preferencia local guardada en disco, evitando parpadeos.
                    }
                } catch (e: Exception) { }

                // 2. Heartbeat: Mantiene el estado "Online" activo
                while (true) {
                    repo.sendHeartbeat(myId)
                    delay(60_000)
                }
            }
        }

        TabNavigator(HomeTab) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = themeColor,
                        tonalElevation = 8.dp
                    ) {
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(ChatTab)
                        TabNavigationItem(AddPostTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CurrentTab()
                }
            }
        }
    }
}

// Helper para items de navegación con estilo personalizado
@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab
    val themeColor = AppCache.themeColor

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { icon ->
                Icon(
                    painter = icon,
                    contentDescription = tab.options.title,
                    tint = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        label = {
            Text(
                text = tab.options.title,
                color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = themeColor,
            selectedTextColor = themeColor,
            indicatorColor = themeColor.copy(alpha = 0.15f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    )
}

// --- DEFINICIÓN DE PESTAÑAS (TABS) ---

object HomeTab : Tab {
    override val options: TabOptions @Composable get() {
        val icon = rememberVectorPainter(Icons.Default.Home)
        return remember { TabOptions(index = 0u, title = "Inicio", icon = icon) }
    }
    @Composable override fun Content() {
        // Navigator anidado para permitir navegación dentro de la pestaña
        Navigator(HomeScreen()) { SlideTransition(it) }
    }
}

object ChatTab : Tab {
    override val options: TabOptions @Composable get() {
        val icon = rememberVectorPainter(Icons.Default.Chat)
        return remember { TabOptions(index = 1u, title = "Chats", icon = icon) }
    }
    @Composable override fun Content() {
        Navigator(ChatListScreen()) { SlideTransition(it) }
    }
}

object AddPostTab : Tab {
    override val options: TabOptions @Composable get() {
        val icon = rememberVectorPainter(Icons.Default.AddBox)
        return remember { TabOptions(index = 2u, title = "Crear", icon = icon) }
    }
    @Composable override fun Content() {
        Navigator(CreatePostScreen()) { SlideTransition(it) }
    }
}

object ProfileTab : Tab {
    override val options: TabOptions @Composable get() {
        val icon = rememberVectorPainter(Icons.Default.Person)
        return remember { TabOptions(index = 3u, title = "Perfil", icon = icon) }
    }
    @Composable override fun Content() {
        val authRepo = remember { AuthRepository() }
        val myId = authRepo.getCurrentUserId() ?: ""
        // Navegación anidada para que pantallas como Settings se abran correctamente
        Navigator(ProfileScreen(userId = myId, isExternal = false)) { SlideTransition(it) }
    }
}