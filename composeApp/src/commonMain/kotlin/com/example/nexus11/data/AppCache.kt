package com.example.nexus11.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.nexus11.data.model.Mensaje
import com.example.nexus11.data.model.Post
import com.example.nexus11.data.model.User
import com.russhwolf.settings.Settings

object AppCache {
    val settings = Settings()

    var users = mutableStateMapOf<String, User>()
    var posts = mutableStateListOf<Post>()
    var bitmapCache = mutableStateMapOf<String, ImageBitmap>()
    var chatList = listOf<Pair<String, Mensaje>>()
    var messagesCache = mutableStateMapOf<String, List<Mensaje>>()
    var myFollowingIds = mutableStateListOf<String>()

    private const val COLOR_KEY_STR = "local_theme_color_v5"
    private const val WALLPAPER_KEY = "local_wallpaper_id_v5"

    private val defaultColorHex = "FF2196F3"

    private val savedColorHex = settings.getString(COLOR_KEY_STR, defaultColorHex)

    // 🔴 CAMBIO AQUÍ: Añadido .toInt() al final.
    // Explicación: Color(Long) falla con colores ARGB normales.
    // Al pasarlo a Int, usamos el constructor Color(Int) que sí funciona.
    var themeColor by mutableStateOf(
        try {
            Color(savedColorHex.toLong(16).toInt())
        } catch (e: Exception) {
            Color(0xFF2196F3)
        }
    )

    var wallpaperStyle by mutableStateOf(settings.getInt(WALLPAPER_KEY, 0))
}