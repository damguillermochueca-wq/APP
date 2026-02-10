package com.example.nexus11.data.model // O el paquete donde lo tengas

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null
)