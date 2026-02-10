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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Story(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val timestamp: Long = 0L
)

class DataRepository {

    private val dbUrl = "https://nexus11-8388b-default-rtdb.europe-west1.firebasedatabase.app"

    // 🔑 PEGA AQUÍ TU API KEY DE IMGBB QUE COPIASTE
    private val imgbbApiKey = "c70d3ff45f1e6a410a7acc118d9f1146"

    private val client = HttpClient {
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
    }

    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            // ✅ CORRECCIÓN: Usamos la función nativa de Ktor que sirve para iOS y Android
            val base64Image = imageBytes.encodeBase64()

            // 2. La enviamos a ImgBB
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

                println("✅ Imagen subida a ImgBB: $url")
                url
            } else {
                println("❌ Error ImgBB: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("❌ Error subiendo imagen: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // --- EL RESTO DE FUNCIONES SIGUE IGUAL ---

    suspend fun getAllPosts(): List<Post> {
        return try {
            val response = client.get("$dbUrl/posts.json")
            if (response.status == HttpStatusCode.OK && response.bodyAsText() != "null") {
                val map = response.body<Map<String, Post>>()
                map.values.sortedByDescending { it.timestamp }.toList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun createPost(post: Post) {
        try {
            client.put("$dbUrl/posts/${post.id}.json") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ... (Mantén aquí tus funciones de Stories, Users y Chat igual que antes) ...
    // Para no hacer el código gigante, asumo que las tienes del paso anterior.
    // Si necesitas que te las pegue todas otra vez, dímelo.

    suspend fun getAllStories(): List<Story> = emptyList() // ⚠️ Rellena con tu código anterior
    suspend fun createStory(story: Story) {} // ⚠️ Rellena con tu código anterior
    suspend fun getAllUsers(): List<User> = emptyList() // ⚠️ Rellena con tu código anterior
    suspend fun getUser(userId: String): User? = null // ⚠️ Rellena con tu código anterior
    suspend fun getMessages(chatId: String): List<Message> = emptyList() // ⚠️ Rellena
    suspend fun sendMessage(chatId: String, message: Message) {} // ⚠️ Rellena
}