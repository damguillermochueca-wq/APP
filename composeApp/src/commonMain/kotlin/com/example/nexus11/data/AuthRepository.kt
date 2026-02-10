package com.example.nexus11.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.datetime.Clock
import com.russhwolf.settings.Settings // Asegúrate de tener este import si usas Settings

class AuthRepository {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // ⚠️ RECUERDA PONER TU API KEY REAL AQUÍ O NO FUNCIONARÁ EL LOGIN
    private val apiKey = "TU_API_KEY_DE_FIREBASE"
    private val firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts"
    private val dbUrl = "https://TU_PROYECTO.firebaseio.com" // Tu URL de Realtime Database

    // Para guardar la sesión (opcional, pero recomendado)
    private val settings = Settings()

    suspend fun signUp(email: String, pass: String, username: String): String? {
        return try {
            val bodyData = buildJsonObject {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }

            val response = httpClient.post("$firebaseAuthUrl:signUp?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(bodyData)
            }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.body<JsonObject>()
                val localId = jsonResponse["localId"]?.jsonPrimitive?.content
                    ?: throw Exception("No localId found")

                val timestamp = Clock.System.now().toEpochMilliseconds()

                val userProfile = buildJsonObject {
                    put("id", localId)
                    put("username", username)
                    put("email", email)
                    put("joinedAt", timestamp)
                    put("profileImageUrl", "")
                }

                httpClient.put("$dbUrl/users/$localId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(userProfile)
                }

                // Guardamos sesión
                settings.putString("current_user_id", localId)
                localId
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error signUp: ${e.message}")
            null
        }
    }

    // ✅ RENOMBRADO: De 'signIn' a 'login' para que LoginScreen no falle
    suspend fun login(email: String, pass: String): String? {
        return try {
            val bodyData = buildJsonObject {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }

            val response = httpClient.post("$firebaseAuthUrl:signInWithPassword?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(bodyData)
            }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.body<JsonObject>()
                val userId = jsonResponse["localId"]?.jsonPrimitive?.content

                // Guardamos sesión si el login es correcto
                if (userId != null) {
                    settings.putString("current_user_id", userId)
                }
                userId
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error login: ${e.message}")
            null
        }
    }

    // ✅ AÑADIDO: Función logout que faltaba
    fun logout() {
        settings.remove("current_user_id")
    }

    fun getCurrentUserId(): String? {
        return settings.getStringOrNull("current_user_id")
    }
}