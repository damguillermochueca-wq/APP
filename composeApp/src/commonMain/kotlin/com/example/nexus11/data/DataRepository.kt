package com.example.nexus11.data

import Mensaje
import com.example.nexus11.data.model.Post
import com.example.nexus11.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class DataRepository {
    // ⚠️ Asegúrate de que esta URL es correcta y tu base de datos tiene reglas de lectura/escritura abiertas (o Auth configurado)
    private val dbUrl = "https://nexus11-8388b-default-rtdb.europe-west1.firebasedatabase.app"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    // ----------------------------------------------------------------
    // 👤 USUARIOS (Profile & Search)
    // ----------------------------------------------------------------

    suspend fun getUser(userId: String): User? {
        return try {
            client.get("$dbUrl/users/$userId.json").body()
        } catch (e: Exception) {
            println("Error getUser: ${e.message}")
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val response: Map<String, User>? = client.get("$dbUrl/users.json").body()
            response?.values?.toList() ?: emptyList()
        } catch (e: Exception) {
            println("Error getAllUsers: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveUser(user: User) {
        try {
            client.put("$dbUrl/users/${user.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
        } catch (e: Exception) {
            println("Error saveUser: ${e.message}")
        }
    }

    // ✅ Actualizar solo la foto de perfil del usuario
    suspend fun updateUserAvatar(userId: String, imageBytes: ByteArray) {
        val imageUrl = uploadImage(imageBytes) // Reutilizamos tu subida blindada
        if (imageUrl != null) {
            try {
                client.patch("$dbUrl/users/$userId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("profileImageUrl" to imageUrl))
                }
            } catch (e: Exception) {
                println("Error actualizando avatar: ${e.message}")
            }
        }
    }

    // ----------------------------------------------------------------
    // 📝 PUBLICACIONES (Feed & Create Post)
    // ----------------------------------------------------------------

    suspend fun getAllPosts(): List<Post> {
        return try {
            val response: Map<String, Post>? = client.get("$dbUrl/posts.json").body()
            response?.map { entry ->
                entry.value.copy(id = entry.key)
            }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            println("Error getAllPosts: ${e.message}")
            emptyList()
        }
    }

    suspend fun createPost(post: Post) {
        try {
            client.put("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) {
            println("Error createPost: ${e.message}")
        }
    }

    suspend fun likePost(post: Post) {
        try {
            client.patch("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("likes" to post.likes + 1))
            }
        } catch (e: Exception) {
            println("Error likePost: ${e.message}")
        }
    }

    suspend fun commentPost(post: Post, commentText: String, username: String) {
        try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val commentId = "c_$timestamp"
            val finalContent = "$username: $commentText"

            client.patch("$dbUrl/posts/${post.id}/comments.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(commentId to finalContent))
            }
        } catch (e: Exception) {
            println("Error commentPost: ${e.message}")
        }
    }

    // ----------------------------------------------------------------
    // 💬 CHATS (Mensajería)
    // ----------------------------------------------------------------

    // ✅ NUEVO: Generar ID único para chats privados (A_B o B_A)
    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }

    suspend fun getAllChats(): List<Pair<String, Mensaje>> {
        return try {
            val response: Map<String, Map<String, Map<String, Mensaje>>>? =
                client.get("$dbUrl/chats.json").body()

            response?.map { (chatId, chatData) ->
                val lastMsg = chatData["messages"]?.values
                    ?.sortedBy { it.timestamp }
                    ?.lastOrNull()
                    ?: Mensaje(text = "Sin mensajes")

                chatId to lastMsg
            }?.sortedByDescending { it.second.timestamp } ?: emptyList()
        } catch (e: Exception) {
            println("Error getAllChats: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMessages(chatId: String): List<Mensaje> {
        return try {
            val response: Map<String, Mensaje>? =
                client.get("$dbUrl/chats/$chatId/messages.json").body()
            response?.values?.sortedBy { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            println("Error getMessages: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(chatId: String, mensaje: Mensaje) {
        try {
            // Usamos POST para que Firebase genere una key única cronológica
            client.post("$dbUrl/chats/$chatId/messages.json") {
                contentType(ContentType.Application.Json)
                setBody(mensaje)
            }
        } catch (e: Exception) {
            println("Error sendMessage: ${e.message}")
        }
    }

    // ----------------------------------------------------------------
    // 📸 IMÁGENES (Subida Base64 optimizada)
    // ----------------------------------------------------------------

    suspend fun uploadImage(imageBytes: ByteArray): String? = withContext(Dispatchers.Default) {
        try {
            val base64 = imageBytes.encodeBase64()
            val cleanBase64 = base64.replace("\n", "").replace("\r", "").trim()
            "data:image/jpeg;base64,$cleanBase64"
        } catch (e: Exception) {
            println("Error subiendo imagen: ${e.message}")
            null
        }
    }
}