package com.example.nexus11.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- MODELOS DE DATOS (Data Transfer Objects) ---

@Serializable
data class ChatPreview(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)

@Serializable
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

/**
 * Repositorio específico para la lógica de Chat en tiempo real.
 */
class ChatRepository {
    private val dbUrl = "https://nexus11-f9c34-default-rtdb.europe-west1.firebasedatabase.app"

    // CLIENTE HTTP CONFIGURADO PARA MULTIPLATAFORMA
    private val client = HttpClient {
        // SOLUCIÓN IOS: El header "Accept-Encoding: identity" es obligatorio para evitar
        // errores de "Content-Length mismatch" que ocurren específicamente en el motor Darwin (iOS).
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

    // Obtención de mensajes con ordenación cronológica en cliente.
    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val response = client.get("$dbUrl/messages/$chatId.json")
            if (response.status == HttpStatusCode.OK) {
                val map = response.body<Map<String, Message>?>() ?: emptyMap()
                map.values.sortedBy { it.timestamp }.toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error mensajes: ${e.message}")
            emptyList()
        }
    }

    // Envío de mensaje en dos pasos: Generación de ID y actualización del payload.
    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val timestamp = io.ktor.util.date.getTimeMillis()

        // 1. Obtener ID único desde Firebase (push key)
        val msgRef = client.post("$dbUrl/messages/$chatId.json") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("temp" to "temp"))
        }
        val msgId = msgRef.body<Map<String, String>>()["name"] ?: return

        // 2. Guardar el mensaje con su ID real
        val newMessage = Message(msgId, senderId, text, timestamp)

        client.put("$dbUrl/messages/$chatId/$msgId.json") {
            contentType(ContentType.Application.Json)
            setBody(newMessage)
        }
    }
}