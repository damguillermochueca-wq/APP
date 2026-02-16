package com.example.nexus11.data

import com.example.nexus11.utils.SecurityUtils
import com.russhwolf.settings.Settings
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
import kotlinx.serialization.json.*

class AuthRepository {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private val apiKey = "AIzaSyBpF8n7JM0Au_6QXKu8kFdLL2rAyJoexBg"
    private val dbUrl = "https://nexus11-v2-default-rtdb.europe-west1.firebasedatabase.app"
    private val firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts"

    private val settings: Settings = Settings()
    private val USER_KEY = "current_user_id"
    private val TOKEN_KEY = "auth_token" // ✅ NUEVA LLAVE PARA EL TOKEN

    fun saveAuthData(id: String, token: String) {
        settings.putString(USER_KEY, id)
        settings.putString(TOKEN_KEY, token) // ✅ Guardamos la llave de acceso
    }

    fun getCurrentUserId(): String? = settings.getStringOrNull(USER_KEY)
    fun getAuthToken(): String? = settings.getStringOrNull(TOKEN_KEY) // ✅ Para el DataRepository

    fun logout() {
        settings.remove(USER_KEY)
        settings.remove(TOKEN_KEY)
    }

    suspend fun login(email: String, pass: String): String? {
        return try {
            // ✅ BLINDAJE: Hasheamos antes de enviar
            val securePass = SecurityUtils.hashPassword(pass)

            val bodyData = buildJsonObject {
                put("email", email)
                put("password", securePass)
                put("returnSecureToken", true)
            }
            val response = httpClient.post("$firebaseAuthUrl:signInWithPassword?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(bodyData)
            }

            if (response.status == HttpStatusCode.OK) {
                val resJson = response.body<JsonObject>()
                val localId = resJson["localId"]?.jsonPrimitive?.content
                val idToken = resJson["idToken"]?.jsonPrimitive?.content // ✅ EL TOKEN

                if (localId != null && idToken != null) {
                    saveAuthData(localId, idToken)
                    localId
                } else null
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun signUp(email: String, pass: String, username: String): String? {
        return try {
            val securePass = SecurityUtils.hashPassword(pass)

            val bodyData = buildJsonObject {
                put("email", email)
                put("password", securePass)
                put("returnSecureToken", true)
            }
            val response = httpClient.post("$firebaseAuthUrl:signUp?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(bodyData)
            }
            if (response.status == HttpStatusCode.OK) {
                val resJson = response.body<JsonObject>()
                val localId = resJson["localId"]?.jsonPrimitive?.content ?: return null
                val idToken = resJson["idToken"]?.jsonPrimitive?.content ?: return null

                // Creamos el perfil inicial
                val userProfile = buildJsonObject {
                    put("id", localId)
                    put("username", username)
                    put("email", email)
                    put("joinedAt", Clock.System.now().toEpochMilliseconds())
                    put("themeColorHex", 0xFF2196F3) // Color por defecto
                }

                // ✅ IMPORTANTE: Usamos el token para poder escribir en la DB protegida
                httpClient.put("$dbUrl/users/$localId.json?auth=$idToken") {
                    contentType(ContentType.Application.Json)
                    setBody(userProfile)
                }

                saveAuthData(localId, idToken)
                localId
            } else null
        } catch (e: Exception) { null }
    }
}