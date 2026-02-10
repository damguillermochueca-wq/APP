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

// --- MODELOS DE DATOS (Cópialos aquí mismo) ---

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

// --- EL REPOSITORIO ---

class ChatRepository {
    // Tu URL de Firebase
    private val dbUrl = "https://nexus11-f9c34-default-rtdb.europe-west1.firebasedatabase.app"

    // ✅ CLIENTE SEGURO (CON LA VACUNA PARA IOS)
    private val client = HttpClient {
        // 💉 ESTO ES OBLIGATORIO: Evita el error "Content-Length mismatch" en iPhone
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

    // 1. Obtener mensajes de un chat específico
    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val response = client.get("$dbUrl/messages/$chatId.json")
            if (response.status == HttpStatusCode.OK) {
                val map = response.body<Map<String, Message>?>() ?: emptyMap()
                // Ordenamos por fecha (el más antiguo primero para leer de arriba a abajo)
                map.values.sortedBy { it.timestamp }.toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error mensajes: ${e.message}")
            emptyList()
        }
    }

    // 2. Enviar un mensaje nuevo
    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val timestamp = io.ktor.util.date.getTimeMillis()

        // A) Guardar el mensaje en la lista de mensajes
        val msgRef = client.post("$dbUrl/messages/$chatId.json") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("temp" to "temp")) // Truco para generar ID
        }
        val msgId = msgRef.body<Map<String, String>>()["name"] ?: return

        val newMessage = Message(msgId, senderId, text, timestamp)

        client.put("$dbUrl/messages/$chatId/$msgId.json") {
            contentType(ContentType.Application.Json)
            setBody(newMessage)
        }

        // B) (Opcional) Actualizar la "última conversación" para que salga en la lista de chats
        // Esto requeriría una estructura de datos 'chats' separada en Firebase
    }
}