package com.example.nexus11

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nexus11.data.AuthRepository
// import com.example.nexus11.data.DataRepository
import com.example.nexus11.ui.screens.splash.SplashScreen
import com.example.nexus11.ui.screens.auth.LoginScreen
import com.example.nexus11.ui.screens.home.HomeScreen
import com.example.nexus11.ui.screens.chat.ChatListScreen
import com.example.nexus11.ui.screens.chat.ChatDetailScreen
import com.example.nexus11.ui.screens.post.CreatePostScreen
import com.example.nexus11.ui.screens.profile.ProfileScreen
import com.example.nexus11.ui.theme.*

// ✅ IMPORTS DE KAMEL (Solo lo necesario)
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.fileFetcher
import io.kamel.core.config.httpFetcher
import io.kamel.core.config.httpUrlFetcher
import io.kamel.image.config.LocalKamelConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

@Composable
fun App() {
    // 🛠️ CONFIGURACIÓN KAMEL (Limpia: Solo Galería e Internet)
    val kamelConfig = remember {
        KamelConfig {
            // 1. Para ver la foto de la Galería antes de subirla
            fileFetcher()

            httpUrlFetcher(HttpClient {
                // 🚀 ESTO ARREGLA EL CRASH: Instalamos el plugin explícitamente
                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000
                    connectTimeoutMillis = 30_000
                }

                defaultRequest {
                    header(HttpHeaders.AcceptEncoding, "identity")
                }
            }
            )
        }
    }

    val navController = rememberNavController()
    val authRepo = remember { AuthRepository() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
        NexusTheme {
            Scaffold(
                containerColor = NexusBlack,
                bottomBar = {
                    if (currentRoute == "home" || currentRoute == "chat_list" || currentRoute?.startsWith("profile") == true) {
                        NavigationBar(containerColor = NexusBlack) {
                            NavigationBarItem(
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") },
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Inicio") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = NexusBlue, indicatorColor = NexusDarkGray, unselectedIconColor = TextGray)
                            )
                            NavigationBarItem(
                                selected = currentRoute == "chat_list",
                                onClick = { navController.navigate("chat_list") },
                                icon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                                label = { Text("Chats") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = NexusBlue, indicatorColor = NexusDarkGray, unselectedIconColor = TextGray)
                            )
                            NavigationBarItem(
                                selected = currentRoute?.startsWith("profile") == true,
                                onClick = { navController.navigate("profile/me") },
                                icon = { Icon(Icons.Default.Person, null) },
                                label = { Text("Perfil") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = NexusBlue, indicatorColor = NexusDarkGray, unselectedIconColor = TextGray)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("splash") {
                        SplashScreen(authRepo) { isLoggedIn ->
                            navController.navigate(if (isLoggedIn) "home" else "login") { popUpTo("splash") { inclusive = true } }
                        }
                    }
                    composable("login") {
                        LoginScreen { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
                    }
                    composable("home") {
                        HomeScreen(
                            onNavigateCreatePost = { navController.navigate("create_post") },
                            onNavigateToUser = { userId -> navController.navigate("profile/$userId") }
                        )
                    }
                    composable("chat_list") {
                        ChatListScreen(
                            onBack = { navController.navigate("home") },
                            onChatClick = { chatId, userId, userName -> navController.navigate("chat_detail/$chatId/$userId/$userName") }
                        )
                    }
                    composable(
                        "profile/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { entry ->
                        ProfileScreen(
                            userIdToShow = entry.arguments?.getString("userId") ?: "me",
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            onNavigateToChat = { chatId, userName ->
                                navController.navigate("chat_detail/$chatId/unknown/$userName")
                            }
                        )
                    }
                    composable("create_post") {
                        CreatePostScreen(
                            onBack = { navController.popBackStack() },
                            onPostCreated = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        "chat_detail/{chatId}/{userId}/{userName}",
                        arguments = listOf(
                            navArgument("chatId") { type = NavType.StringType },
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { entry ->
                        ChatDetailScreen(
                            chatId = entry.arguments?.getString("chatId") ?: "",
                            otherUserId = entry.arguments?.getString("userId") ?: "",
                            userName = entry.arguments?.getString("userName") ?: "",
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}