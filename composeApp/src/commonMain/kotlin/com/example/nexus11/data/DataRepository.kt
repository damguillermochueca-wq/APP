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
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import io.ktor.util.encodeBase64

class DataRepository {
    private val dbUrl = "https://nexus11-8388b-default-rtdb.europe-west1.firebasedatabase.app"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // --- USUARIOS ---
    suspend fun getUser(userId: String): User? = try {
        client.get("$dbUrl/users/$userId.json").body()
    } catch (e: Exception) { null }

    // --- PUBLICACIONES (POSTS) ---
    suspend fun getAllPosts(): List<Post> {
        return try {
            val response: Map<String, Post>? = client.get("$dbUrl/posts.json").body()
            response?.map { it.value.copy(id = it.key) }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun createPost(post: Post) {
        try {
            client.put("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) {}
    }

    suspend fun likePost(post: Post) {
        try {
            client.patch("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("likes" to post.likes + 1))
            }
        } catch (e: Exception) {}
    }

    // ✅ COMENTARIOS (SISTEMA PATCH)
    suspend fun commentPost(post: Post, commentText: String, username: String) {
        try {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val commentId = "c_$timestamp"
            val finalContent = "$username: $commentText"

            client.patch("$dbUrl/posts/${post.id}/comments.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(commentId to finalContent))
            }
        } catch (e: Exception) {}
    }

    // --- CHAT (MENSAJES) ---

    // 1. Obtener todos los chats para la lista (ChatList)
    suspend fun getAllChats(): List<Pair<String, Mensaje>> {
        return try {
            val response: Map<String, Map<String, Map<String, Mensaje>>>? =
                client.get("$dbUrl/chats.json").body()

            response?.map { (chatId, chatData) ->
                val lastMsg = chatData["messages"]?.values?.sortedBy { it.timestamp }?.lastOrNull()
                    ?: Mensaje(text = "Sin mensajes")
                chatId to lastMsg
            }?.sortedByDescending { it.second.timestamp } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // 2. Obtener los mensajes de un chat específico
    suspend fun getMessages(chatId: String): List<Mensaje> {
        return try {
            val response: Map<String, Mensaje>? =
                client.get("$dbUrl/chats/$chatId/messages.json").body()
            response?.values?.sortedBy { it.timestamp } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // 3. Enviar un mensaje
    suspend fun sendMessage(chatId: String, mensaje: Mensaje) {
        try {
            client.post("$dbUrl/chats/$chatId/messages.json") {
                contentType(ContentType.Application.Json)
                setBody(mensaje)
            }
        } catch (e: Exception) {}
    }

    // --- IMÁGENES ---
    suspend fun uploadImage(imageBytes: ByteArray): String? = try {
        "data:image/jpeg;base64,${imageBytes.encodeBase64()}"
    } catch (e: Exception) { null }
}