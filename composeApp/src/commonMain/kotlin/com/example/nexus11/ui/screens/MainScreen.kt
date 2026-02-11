package com.example.nexus11.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox // Icono para añadir
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.ui.screens.chat.ChatListScreen
import com.example.nexus11.ui.screens.home.HomeScreen
import com.example.nexus11.ui.screens.post.CreatePostScreen
import com.example.nexus11.ui.screens.profile.ProfileScreen
import com.example.nexus11.ui.theme.NexusBlue

class MainScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(HomeTab) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = NexusBlue,
                        tonalElevation = 0.dp // Quitamos elevación para que se fusione con el fondo
                    ) {
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(ChatTab)
                        TabNavigationItem(AddPostTab) // ✅ AÑADIDO EN MEDIO
                        TabNavigationItem(ProfileTab)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab
    val selectedColor = NexusBlue
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = tab.options.title,
                tint = if (isSelected) selectedColor else unselectedColor
            )
        },
        // Opcional: Si quieres quitar los textos de abajo para que sea más limpio, comenta la línea de label
        label = { Text(tab.options.title, color = if (isSelected) selectedColor else unselectedColor) },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )
}

// --- PESTAÑAS ---

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Inicio", icon = icon) }
        }
    @Composable override fun Content() { HomeScreen().Content() }
}

object ChatTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Chat)
            return remember { TabOptions(index = 1u, title = "Chats", icon = icon) }
        }
    @Composable override fun Content() { ChatListScreen().Content() }
}

// ✅ NUEVA PESTAÑA PARA CREAR POST
object AddPostTab : Tab {
    override val options: TabOptions
        @Composable get() {
            // Usamos un icono de "caja con más" que queda bien en el centro
            val icon = rememberVectorPainter(Icons.Default.AddBox)
            return remember { TabOptions(index = 2u, title = "Crear", icon = icon) }
        }
    @Composable override fun Content() {
        // Cargamos directamente la pantalla de crear
        CreatePostScreen().Content()
    }
}

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember { TabOptions(index = 3u, title = "Perfil", icon = icon) }
        }
    @Composable override fun Content() {
        val authRepo = remember { AuthRepository() }
        val myId = authRepo.getCurrentUserId() ?: ""
        ProfileScreen(myId).Content()
    }
}