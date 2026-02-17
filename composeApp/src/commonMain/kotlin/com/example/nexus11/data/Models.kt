package com.example.nexus11.data

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