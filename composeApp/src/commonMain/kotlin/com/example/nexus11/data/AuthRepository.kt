package com.example.nexus11.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
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
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AuthRepository {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    // ✅ TUS DATOS REALES (EXTRAÍDOS DE TU JSON)
    private val apiKey = "AIzaSyATeUpvFKJH7Kzf3LsU7sQOQF7wxZGUA9U"
    private val dbUrl = "https://nexus11-8388b-default-rtdb.europe-west1.firebasedatabase.app"

    private val firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts"
    private val settings: Settings = Settings()
    private val USER_KEY = "current_user_id"

    fun saveUserId(id: String) { settings.set(USER_KEY, id) }
    fun getCurrentUserId(): String? = settings.getStringOrNull(USER_KEY)
    fun logout() = settings.remove(USER_KEY)

    suspend fun login(email: String, pass: String): String? {
        return try {
            val bodyData = buildJsonObject {
                put("email", email); put("password", pass); put("returnSecureToken", true)
            }
            // Importante: quitamos los dos puntos extra en la URL que a veces dan problemas
            val response = httpClient.post("$firebaseAuthUrl:signInWithPassword?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(bodyData)
            }

            if (response.status == HttpStatusCode.OK) {
                val json = response.body<JsonObject>()
                val localId = json["localId"]?.jsonPrimitive?.content
                if (localId != null) saveUserId(localId)
                localId
            } else {
                println("⚠️ ERROR LOGIN: ${response.body<String>()}")
                null
            }
        } catch (e: Exception) {
            println("❌ EXCEPCIÓN LOGIN: ${e.message}")
            null
        }
    }

    suspend fun signUp(email: String, pass: String, username: String): String? {
        return try {
            val bodyData = buildJsonObject {
                put("email", email); put("password", pass); put("returnSecureToken", true)
            }
            val response = httpClient.post("$firebaseAuthUrl:signUp?key=$apiKey") {
                contentType(ContentType.Application.Json); setBody(bodyData)
            }
            if (response.status == HttpStatusCode.OK) {
                val json = response.body<JsonObject>()
                val localId = json["localId"]?.jsonPrimitive?.content ?: return null

                val timestamp = Clock.System.now().toEpochMilliseconds()
                val userProfile = buildJsonObject {
                    put("id", localId); put("username", username); put("email", email)
                    put("joinedAt", timestamp); put("profileImageUrl", "")
                }
                httpClient.put("$dbUrl/users/$localId.json") {
                    contentType(ContentType.Application.Json); setBody(userProfile)
                }
                saveUserId(localId)
                localId
            } else {
                println("⚠️ ERROR SIGNUP: ${response.body<String>()}")
                null
            }
        } catch (e: Exception) { null }
    }
}