package com.example.nexus11.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.nexus11.ui.screens.chat.ChatListScreen
import com.example.nexus11.ui.screens.home.HomeScreen
import com.example.nexus11.ui.theme.NexusBlack
import com.example.nexus11.ui.theme.NexusBlue
import com.example.nexus11.ui.theme.NexusDarkGray
import com.example.nexus11.ui.theme.TextGray

class MainScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(HomeTab) {
            Scaffold(
                containerColor = NexusBlack,
                bottomBar = {
                    NavigationBar(
                        containerColor = NexusBlack,
                        contentColor = NexusBlue
                    ) {
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(ChatTab)
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

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = tab.options.title,
                tint = if (tabNavigator.current == tab) NexusBlue else TextGray
            )
        },
        label = {
            Text(
                text = tab.options.title,
                color = if (tabNavigator.current == tab) NexusBlue else TextGray
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = NexusDarkGray
        )
    )
}

// --- PESTAÑAS ---

object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Home", icon = icon) }
        }

    @Composable
    override fun Content() {
        HomeScreen().Content()
    }
}

object ChatTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Chat)
            return remember { TabOptions(index = 1u, title = "Chats", icon = icon) }
        }

    @Composable
    override fun Content() {
        ChatListScreen().Content()
    }
}