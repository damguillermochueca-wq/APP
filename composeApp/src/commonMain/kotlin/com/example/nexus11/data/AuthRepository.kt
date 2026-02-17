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

/**
 * Repositorio encargado de la Autenticación y Seguridad.
 * Gestiona el ciclo de vida de la sesión (Login/Register/Logout) contra Firebase Auth.
 */
class AuthRepository {

    // Cliente HTTP configurado con serialización JSON tolerante a fallos.
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    // Configuración de endpoints de Firebase (REST API)
    private val apiKey = "AIzaSyBpF8n7JM0Au_6QXKu8kFdLL2rAyJoexBg"
    private val dbUrl = "https://nexus11-v2-default-rtdb.europe-west1.firebasedatabase.app"
    private val firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts"

    // Persistencia segura local para mantener la sesión activa.
    private val settings: Settings = Settings()
    private val USER_KEY = "current_user_id"
    private val TOKEN_KEY = "auth_token" // Token JWT para validar peticiones en la DB.

    fun saveAuthData(id: String, token: String) {
        settings.putString(USER_KEY, id)
        settings.putString(TOKEN_KEY, token)
    }

    fun getCurrentUserId(): String? = settings.getStringOrNull(USER_KEY)

    // Método para inyectar el token en las llamadas del DataRepository.
    fun getAuthToken(): String? = settings.getStringOrNull(TOKEN_KEY)

    fun logout() {
        settings.remove(USER_KEY)
        settings.remove(TOKEN_KEY)
    }

    /**
     * Inicia sesión validando credenciales.
     * SEGURIDAD: La contraseña se hashea (SHA-256) en el cliente antes de viajar por la red.
     */
    suspend fun login(email: String, pass: String): String? {
        return try {
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
                val idToken = resJson["idToken"]?.jsonPrimitive?.content

                if (localId != null && idToken != null) {
                    saveAuthData(localId, idToken)
                    localId
                } else null
            } else null
        } catch (e: Exception) { null }
    }

    /**
     * Registro de nuevo usuario.
     * Crea la cuenta en Auth y posteriormente inicializa la entrada del usuario en la Realtime Database.
     */
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

                // Creación del perfil inicial en base de datos.
                val userProfile = buildJsonObject {
                    put("id", localId)
                    put("username", username)
                    put("email", email)
                    put("joinedAt", Clock.System.now().toEpochMilliseconds())
                    put("themeColorHex", 0xFF2196F3)
                }

                // Uso del token (?auth=...) para tener permiso de escritura en nodos protegidos.
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