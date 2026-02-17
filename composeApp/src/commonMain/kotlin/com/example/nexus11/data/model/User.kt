package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val bio: String = "",
    val profession: String = "",
    val hobby: String = "",
    val status: String = "",
    val lastActive: Long = 0,
    val allowNotifications: Boolean = true,
    val showActivityStatus: Boolean = true,
    val biometricEnabled: Boolean = false,
    val themeColorHex: Long = 0xFF2196F3, // Azul por defecto
    val wallpaperId: Int = 0
)