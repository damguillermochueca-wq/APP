package com.example.nexus11.data

import androidx.compose.ui.graphics.Color
import com.example.nexus11.data.model.Mensaje
import com.example.nexus11.data.model.Post
import com.example.nexus11.data.model.Story
import com.example.nexus11.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*

/**
 * Repositorio Central de Datos.
 * Gestiona toda la lógica de negocio, red y sincronización con Firebase Database.
 * Integra la seguridad mediante la inyección automática del token de sesión.
 */
class DataRepository {
    private val dbUrl = "https://nexus11-v2-default-rtdb.europe-west1.firebasedatabase.app/"

    // Dependencia del AuthRepository para recuperar credenciales seguras.
    private val authRepo = AuthRepository()

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    // CAPA DE SEGURIDAD: Método helper que adjunta el token JWT a cada petición.
    // Esto es necesario para cumplir las Reglas de Seguridad de Firebase (Read/Write auth != null).
    private fun getAuthUrl(path: String): String {
        val token = authRepo.getAuthToken()
        return if (token != null) "$dbUrl/$path?auth=$token" else "$dbUrl/$path"
    }

    // ----------------------------------------------------------------
    // GESTIÓN DE USUARIOS Y PERFIL
    // ----------------------------------------------------------------

    suspend fun getUser(userId: String): User? {
        return try {
            client.get(getAuthUrl("users/$userId.json")).body()
        } catch (e: Exception) { null }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val response: Map<String, User>? = client.get(getAuthUrl("users.json")).body()
            response?.values?.toList() ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun saveUser(user: User) {
        try {
            client.put(getAuthUrl("users/${user.id}.json")) {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
        } catch (e: Exception) { }
    }

    // Actualización optimizada de avatar: Primero sube la imagen, luego actualiza el link.
    suspend fun updateUserAvatar(userId: String, imageBytes: ByteArray) {
        val imageUrl = uploadImage(imageBytes)
        if (imageUrl != null) {
            try {
                client.patch(getAuthUrl("users/$userId.json")) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("profileImageUrl" to imageUrl))
                }
            } catch (e: Exception) { }
        }
    }

    suspend fun updateProfileInfo(userId: String, profession: String, hobby: String, status: String, bio: String) {
        try {
            client.patch(getAuthUrl("users/$userId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "profession" to profession,
                    "hobby" to hobby,
                    "status" to status,
                    "bio" to bio
                ))
            }
        } catch (e: Exception) { }
    }

    // ----------------------------------------------------------------
    // SISTEMA DE PERSONALIZACIÓN (Fix V5)
    // ----------------------------------------------------------------

    /**
     * Actualiza la apariencia del usuario con persistencia híbrida.
     * 1. Actualiza RAM (AppCache) para feedback instantáneo.
     * 2. Persiste en Disco como HexString para evitar corrupción de datos.
     * 3. Sincroniza con la Nube.
     */
    suspend fun updateUserAppearance(userId: String, colorHex: Long, wallpaper: Int) {
        // 1. RAM
        AppCache.themeColor = Color(colorHex.toInt())
        AppCache.wallpaperStyle = wallpaper

        // 2. DISCO (V5 Hex String)
        val hexString = (colorHex and 0xFFFFFFFFL).toString(16).uppercase()
        AppCache.settings.putString("local_theme_color_v5", hexString)
        AppCache.settings.putInt("local_wallpaper_id_v5", wallpaper)

        // 3. User RAM Update
        val currentUser = AppCache.users[userId]
        if (currentUser != null) {
            AppCache.users[userId] = currentUser.copy(themeColorHex = colorHex, wallpaperId = wallpaper)
        }

        // 4. Firebase (Secured)
        try {
            client.patch(getAuthUrl("users/$userId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("themeColorHex" to colorHex, "wallpaperId" to wallpaper))
            }
        } catch (e: Exception) { }
    }

    // Actualización atómica de configuraciones de privacidad y UI.
    suspend fun updateUserSettings(
        userId: String,
        allowNotif: Boolean,
        showActivity: Boolean,
        biometricEnabled: Boolean,
        themeColorHex: Long
    ) {
        // Actualización RAM y Disco local inmediata
        AppCache.themeColor = Color(themeColorHex.toInt())
        val hexString = (themeColorHex and 0xFFFFFFFFL).toString(16).uppercase()
        AppCache.settings.putString("local_theme_color_v5", hexString)

        val currentUser = AppCache.users[userId]
        if (currentUser != null) {
            AppCache.users[userId] = currentUser.copy(
                allowNotifications = allowNotif,
                showActivityStatus = showActivity,
                biometricEnabled = biometricEnabled,
                themeColorHex = themeColorHex
            )
        }

        // Sincronización Nube
        try {
            client.patch(getAuthUrl("users/$userId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "allowNotifications" to allowNotif,
                    "showActivityStatus" to showActivity,
                    "biometricEnabled" to biometricEnabled,
                    "themeColorHex" to themeColorHex
                ))
            }
        } catch (e: Exception) { }
    }

    // Heartbeat para sistema de presencia (Online/Offline)
    suspend fun sendHeartbeat(userId: String) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            client.patch(getAuthUrl("users/$userId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("lastActive" to now))
            }
        } catch (e: Exception) { }
    }

    // ----------------------------------------------------------------
    // GESTIÓN DE PUBLICACIONES (POSTS)
    // ----------------------------------------------------------------

    suspend fun getAllPosts(): List<Post> {
        return try {
            val response: Map<String, Post>? = client.get(getAuthUrl("posts.json")).body()
            // Mapeo del ID (Key) al objeto y ordenación cronológica inversa.
            response?.map { entry ->
                entry.value.copy(id = entry.key)
            }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun createPost(post: Post) {
        try {
            client.put(getAuthUrl("posts/${post.id}.json")) {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) { }
    }

    suspend fun toggleLikePost(postId: String, userId: String, isAlreadyLiked: Boolean) {
        try {
            if (isAlreadyLiked) {
                client.delete(getAuthUrl("posts/$postId/likedBy/$userId.json"))
            } else {
                client.put(getAuthUrl("posts/$postId/likedBy/$userId.json")) {
                    contentType(ContentType.Application.Json)
                    setBody(true)
                }
            }
        } catch (e: Exception) { }
    }

    suspend fun commentPost(post: Post, commentText: String, username: String) {
        try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val commentId = "c_$timestamp"
            val finalContent = "$username: $commentText"
            client.patch(getAuthUrl("posts/${post.id}/comments.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(commentId to finalContent))
            }
        } catch (e: Exception) { }
    }

    // ----------------------------------------------------------------
    // HISTORIAS EFÍMERAS (STORIES)
    // ----------------------------------------------------------------

    suspend fun getActiveStories(): List<Story> = try {
        val now = Clock.System.now().toEpochMilliseconds()
        val oneDayAgo = now - (24 * 60 * 60 * 1000) // Lógica de caducidad (24h)

        val response: Map<String, Story>? = client.get(getAuthUrl("stories.json")).body()

        response?.map { (key, story) ->
            story.copy(id = key)
        }?.filter { it.timestamp > oneDayAgo }
            ?.sortedByDescending { it.timestamp } ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun uploadStory(story: Story) {
        try {
            client.post(getAuthUrl("stories.json")) {
                contentType(ContentType.Application.Json)
                setBody(story)
            }
        } catch (e: Exception) { }
    }

    // ----------------------------------------------------------------
    // CHATS Y MENSAJERÍA
    // ----------------------------------------------------------------

    // Genera un ID de chat determinista para usuarios únicos (ej: A_B es igual a B_A)
    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }

    suspend fun getMyChats(myId: String): List<Pair<String, Mensaje>> {
        return try {
            val response = client.get(getAuthUrl("chats.json")).body<JsonObject>()

            response.entries.mapNotNull { (chatId, chatElement) ->
                if (!chatId.contains(myId)) return@mapNotNull null
                try {
                    val chatObj = chatElement.jsonObject
                    if (!chatObj.containsKey("messages")) return@mapNotNull null

                    val messagesElement = chatObj["messages"]!!.jsonObject
                    val messages = messagesElement.values.mapNotNull { msgJson ->
                        try { jsonConfig.decodeFromJsonElement<Mensaje>(msgJson) } catch (e: Exception) { null }
                    }

                    if (messages.isEmpty()) return@mapNotNull null

                    val lastMsg = messages.maxByOrNull { it.timestamp }!!
                    chatId to lastMsg

                } catch (e: Exception) { null }
            }.sortedByDescending { it.second.timestamp }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getMessages(chatId: String): List<Mensaje> {
        return try {
            val response = client.get(getAuthUrl("chats/$chatId/messages.json")).body<JsonObject>()

            response.entries.mapNotNull { (key, element) ->
                try {
                    val msg = jsonConfig.decodeFromJsonElement<Mensaje>(element)
                    msg.copy(id = key)
                } catch (e: Exception) { null }
            }.sortedBy { it.timestamp }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun sendMessage(chatId: String, mensaje: Mensaje) {
        try {
            client.post(getAuthUrl("chats/$chatId/messages.json")) {
                contentType(ContentType.Application.Json)
                setBody(mensaje)
            }
        } catch (e: Exception) { }
    }

    suspend fun updateMessage(chatId: String, messageId: String, text: String? = null, isEdited: Boolean? = null, reactions: Map<String, String>? = null) {
        try {
            val jsonBody = buildJsonObject {
                if (text != null) put("text", text)
                if (isEdited != null) put("isEdited", isEdited)
                if (reactions != null) {
                    put("reactions", buildJsonObject {
                        reactions.forEach { (userId, emoji) -> put(userId, emoji) }
                    })
                }
            }
            client.patch(getAuthUrl("chats/$chatId/messages/$messageId.json")) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
        } catch (e: Exception) { }
    }

    suspend fun markMessagesAsRead(chatId: String, otherUserId: String) {
        try {
            val messages = getMessages(chatId)
            val unreadMessages = messages.filter { it.senderId == otherUserId && !it.isRead }
            unreadMessages.forEach { msg ->
                client.patch(getAuthUrl("chats/$chatId/messages/${msg.id}.json")) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("isRead" to true))
                }
            }
        } catch (e: Exception) { }
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        try {
            client.patch(getAuthUrl("chats/$chatId/messages/$messageId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "text" to "🚫 Mensaje eliminado",
                    "imageUrl" to null,
                    "isDeleted" to true
                ))
            }
        } catch (e: Exception) { }
    }

    suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        try {
            client.patch(getAuthUrl("chats/$chatId/typing.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(userId to isTyping))
            }
        } catch (e: Exception) { }
    }

    suspend fun getTypingStatus(chatId: String): Map<String, Boolean> {
        return try {
            val response: Map<String, Boolean>? = client.get(getAuthUrl("chats/$chatId/typing.json")).body()
            response ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
    }

    // ----------------------------------------------------------------
    // SEGUIDORES Y RED SOCIAL
    // ----------------------------------------------------------------

    suspend fun followUser(myId: String, targetId: String) {
        try {
            // Actualización dual: Yo sigo a X / X es seguido por Mí
            client.patch(getAuthUrl("following/$myId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(targetId to true))
            }
            client.patch(getAuthUrl("followers/$targetId.json")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(myId to true))
            }
        } catch (e: Exception) { }
    }

    suspend fun unfollowUser(myId: String, targetId: String) {
        try {
            client.delete(getAuthUrl("following/$myId/$targetId.json"))
            client.delete(getAuthUrl("followers/$targetId/$myId.json"))
        } catch (e: Exception) { }
    }

    suspend fun getMyFollowing(myId: String): Set<String> {
        return try {
            val response: Map<String, Boolean>? = client.get(getAuthUrl("following/$myId.json")).body()
            response?.keys ?: emptySet()
        } catch (e: Exception) { emptySet() }
    }

    suspend fun getFollowStats(userId: String): Pair<Int, Int> {
        return try {
            val followers = getFollowersIds(userId).size
            val following = getFollowingIds(userId).size
            Pair(followers, following)
        } catch (e: Exception) { Pair(0, 0) }
    }

    suspend fun amIFollowing(myId: String, targetId: String): Boolean {
        return try {
            val response: Boolean? = client.get(getAuthUrl("following/$myId/$targetId.json")).body()
            response == true
        } catch (e: Exception) { false }
    }

    suspend fun getFollowersIds(userId: String): List<String> {
        return try {
            val response = client.get(getAuthUrl("followers/$userId.json"))
            val text = response.bodyAsText()
            if (text == "null" || text.isBlank()) return emptyList()
            val map: Map<String, Boolean> = response.body()
            map.keys.toList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getFollowingIds(userId: String): List<String> {
        return try {
            val response = client.get(getAuthUrl("following/$userId.json"))
            val text = response.bodyAsText()
            if (text == "null" || text.isBlank()) return emptyList()
            val map: Map<String, Boolean> = response.body()
            map.keys.toList()
        } catch (e: Exception) { emptyList() }
    }

    // ----------------------------------------------------------------
    // UTILIDADES DE IMAGEN
    // ----------------------------------------------------------------

    /**
     * Convierte bytes a Base64 estándar.
     * Se ejecuta en Dispatchers.Default para no bloquear el hilo principal de la UI.
     */
    suspend fun uploadImage(imageBytes: ByteArray): String? = withContext(Dispatchers.Default) {
        try {
            val base64 = imageBytes.encodeBase64()
            // Limpieza de caracteres de control para compatibilidad multiplataforma
            val cleanBase64 = base64.replace("\n", "").replace("\r", "").trim()
            "data:image/jpeg;base64,$cleanBase64"
        } catch (e: Exception) { null }
    }
}