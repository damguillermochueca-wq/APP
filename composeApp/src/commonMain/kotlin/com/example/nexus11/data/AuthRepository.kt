package com.example.nexus11.data

import com.example.nexus11.getCurrentTimeMillis
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest // 👈 IMPORTANTE
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.*

class AuthRepository {

    // ✅ CLIENTE UNIFICADO
    private val client = HttpClient {

        // 🛠️ SOLUCIÓN PARA IOS:
        // Le decimos al servidor "Mándame los datos sin comprimir (identity)".
        // Así los bytes coinciden exactos y Ktor no se queja en el iPhone.
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

    private val settings = Settings()
    private val apiKey = "AIzaSyATeUpvFKJH7Kzf3LsU7sQOQF7wxZGUA9U"
    private val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts"
    private val dbUrl = "https://nexus11-f9c34-default-rtdb.europe-west1.firebasedatabase.app"

    suspend fun login(email: String, password: String) {
        val response = client.post {
            url("$authUrl:signInWithPassword?key=$apiKey")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("email", email)
                put("password", password)
                put("returnSecureToken", true)
            })
        }

        if (response.status == HttpStatusCode.OK) {
            val json = response.body<JsonObject>()
            val uid = json["localId"]?.jsonPrimitive?.content ?: throw Exception("UID missing")
            settings.putString("current_user_id", uid)
        } else {
            throw Exception("Error login: ${response.bodyAsText()}")
        }
    }

    suspend fun signUp(email: String, password: String, username: String) {
        val response = client.post {
            url("$authUrl:signUp?key=$apiKey")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("email", email)
                put("password", password)
                put("returnSecureToken", true)
            })
        }

        if (response.status == HttpStatusCode.OK) {
            val json = response.body<JsonObject>()
            val uid = json["localId"]?.jsonPrimitive?.content ?: throw Exception("Error UID")
            val timestamp = getCurrentTimeMillis()

            client.put {
                url("$dbUrl/users/$uid.json")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("id", uid)
                    put("username", username)
                    put("email", email)
                    put("profileImageUrl", "")
                    put("createdAt", timestamp)
                })
            }
            settings.putString("current_user_id", uid)
        } else {
            throw Exception("Error registro: ${response.bodyAsText()}")
        }
    }

    fun logout() = settings.remove("current_user_id")
    fun getCurrentUserId() = settings.getStringOrNull("current_user_id")
}