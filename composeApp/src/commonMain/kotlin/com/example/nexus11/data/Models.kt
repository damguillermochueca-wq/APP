package com.example.nexus11.data

// Ya no necesitamos importar el reloj aquí, ¡menos líos!

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val bio: String = ""
)

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String? = null,
    val imageUrl: String?,
    val content: String,

    // 👇 CAMBIO CLAVE: Ponemos 0L por defecto.
    // La hora real la calculamos en el DataRepository al crear el post.
    val timestamp: Long = 0L,

    val likes: Int = 0
)

data class Chat(
    val id: String,
    val participants: List<String>,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0
)