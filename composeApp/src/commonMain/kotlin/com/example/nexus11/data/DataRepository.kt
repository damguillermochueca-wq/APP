package com.example.nexus11.data

import io.ktor.util.encodeBase64
import com.example.nexus11.data.model.User
import com.example.nexus11.data.model.Message
import com.example.nexus11.data.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Modelo simple para las historias (Stories)
@Serializable
data class Story(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val timestamp: Long = 0L
)

class DataRepository {

    // ⚠️ Asegúrate de que esta URL es EXACTA a la de tu consola de Firebase
    private val dbUrl = "https://nexus11-8388b-default-rtdb.europe-west1.firebasedatabase.app"

    // 🔑 TU API KEY DE IMGBB
    private val imgbbApiKey = "c70d3ff45f1e6a410a7acc118d9f1146"

    private val client = HttpClient {
        // Esto ayuda a evitar problemas de compresión en Android
        defaultRequest {
            header(HttpHeaders.AcceptEncoding, "identity")
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        // ❌ NO AÑADIMOS HttpTimeout PARA EVITAR CRASHES
    }

    // --- 📸 SUBIDA DE IMÁGENES ---
    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            // 1. Codificamos la imagen a Base64 (Nativo de Ktor)
            val base64Image = imageBytes.encodeBase64()

            // 2. Enviamos a ImgBB
            val response = client.submitForm(
                url = "https://api.imgbb.com/1/upload?key=$imgbbApiKey",
                formParameters = parameters {
                    append("image", base64Image)
                }
            )

            if (response.status == HttpStatusCode.OK) {
                val responseText = response.bodyAsText()
                val json = Json.parseToJsonElement(responseText).jsonObject
                val data = json["data"]?.jsonObject
                val url = data?.get("url")?.jsonPrimitive?.content
                println("✅ Imagen subida: $url")
                url
            } else {
                println("❌ Error ImgBB: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("❌ Excepción subiendo imagen: ${e.message}")
            null
        }
    }

    // --- 📝 POSTS ---
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

    suspend fun getAllPosts(): List<Post> {
        return try {
            val response = client.get("$dbUrl/posts.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                val map = response.body<Map<String, Post>>()
                map.values.sortedByDescending { it.timestamp }.toList()
            } else emptyList()
        } catch (e: Exception) {
            println("Error getAllPosts: ${e.message}")
            emptyList()
        }
    }

    // --- 🌀 HISTORIAS (STORIES) ---
    suspend fun createStory(story: Story) {
        try {
            client.put("$dbUrl/stories/${story.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(story)
            }
        } catch (e: Exception) {
            println("Error createStory: ${e.message}")
        }
    }

    suspend fun getAllStories(): List<Story> {
        return try {
            val response = client.get("$dbUrl/stories.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                val map = response.body<Map<String, Story>>()
                map.values.sortedByDescending { it.timestamp }.toList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- 👤 USUARIOS ---
    suspend fun getUser(userId: String): User? {
        return try {
            val response = client.get("$dbUrl/users/$userId.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                response.body<User>()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val response = client.get("$dbUrl/users.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                val map = response.body<Map<String, User>>()
                map.values.toList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- 💬 CHAT ---
    suspend fun sendMessage(chatId: String, message: Message) {
        try {
            // Guardamos el mensaje usando su timestamp como ID para ordenarlo fácil
            client.put("$dbUrl/chats/$chatId/${message.timestamp}.json") {
                contentType(ContentType.Application.Json)
                setBody(message)
            }
        } catch (e: Exception) {
            println("Error sendMessage: ${e.message}")
        }
    }

    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val response = client.get("$dbUrl/chats/$chatId.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                val map = response.body<Map<String, Message>>()
                map.values.sortedBy { it.timestamp }.toList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    // ✅ NUEVO: Función para dar Like
    suspend fun likePost(post: Post) {
        try {
            val newLikes = post.likes + 1
            // Actualizamos solo el campo 'likes' en Firebase
            client.patch("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("likes" to newLikes))
            }
        } catch (e: Exception) {
            println("Error dando like: ${e.message}")
        }
    }

    // ✅ NUEVO: Función para Comentar
    suspend fun commentPost(post: Post, commentText: String, username: String) {
        try {
            // 1. Obtenemos el tiempo actual (Forma correcta en KMP)
            val timestamp = Clock.System.now().toEpochMilliseconds()

            // ✅ ESTA ES LA LÍNEA QUE FALTABA:
            // Usamos el timestamp como ID único para el comentario
            val commentId = timestamp.toString()

            val commentContent = "$username: $commentText"

            // Guardamos el comentario dentro del post en Firebase
            client.put("$dbUrl/posts/${post.id}/comments/$commentId.json") {
                contentType(ContentType.Application.Json)
                setBody(commentContent)
            }
        } catch (e: Exception) {
            println("Error comentando: ${e.message}")
        }
    }
}