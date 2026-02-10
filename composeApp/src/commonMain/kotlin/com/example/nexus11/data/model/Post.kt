package com.example.nexus11.data.model

import kotlinx.serialization.Serializable // 👈 ESTE IMPORT ES OBLIGATORIO

@Serializable // 👈 SI NO TIENES ESTO, NO SE GUARDA NADA
data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatarUrl: String? = null,
    val text: String = "",       // Asegúrate de que se llame 'text'
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val likes: Int = 0
)