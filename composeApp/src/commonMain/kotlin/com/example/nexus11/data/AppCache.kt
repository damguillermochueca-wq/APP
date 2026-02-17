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

/**
 * Singleton de Gestión de Estado Global (In-Memory Source of Truth).
 * Actúa como una caché caliente para la UI, evitando lecturas constantes a disco o red.
 * Utiliza los State de Compose para que la UI reaccione automáticamente a los cambios.
 */
object AppCache {
    // Persistencia ligera clave-valor (MultiplatformSettings) para tokens y config.
    val settings = Settings()

    // --- ESTADO REACTIVO (RAM) ---
    // Mapas y Listas observables para usuarios, posts e imágenes.
    var users = mutableStateMapOf<String, User>()
    var posts = mutableStateListOf<Post>()

    // Caché de Bitmaps decodificados para evitar lag al hacer scroll en listas.
    var bitmapCache = mutableStateMapOf<String, ImageBitmap>()

    var chatList = listOf<Pair<String, Mensaje>>()
    var messagesCache = mutableStateMapOf<String, List<Mensaje>>()
    var myFollowingIds = mutableStateListOf<String>()

    // --- PERSISTENCIA DE CONFIGURACIÓN ---
    private const val COLOR_KEY_STR = "local_theme_color_v5"
    private const val WALLPAPER_KEY = "local_wallpaper_id_v5"

    private val defaultColorHex = "FF2196F3"

    // Recuperamos el color como String Hexadecimal para evitar corrupción de datos entre plataformas.
    private val savedColorHex = settings.getString(COLOR_KEY_STR, defaultColorHex)

    /**
     * Color del Tema Dinámico.
     * SOLUCIÓN TÉCNICA (Fix V5): Se convierte de Hex String -> Long -> Int -> Color.
     * Esto soluciona un bug donde guardar el color directamente como Long/Int provocaba
     * inconsistencias en el canal Alpha en ciertos dispositivos Android/iOS.
     */
    var themeColor by mutableStateOf(
        try {
            Color(savedColorHex.toLong(16).toInt())
        } catch (e: Exception) {
            Color(0xFF2196F3) // Fallback a azul por defecto si falla el parseo
        }
    )

    var wallpaperStyle by mutableStateOf(settings.getInt(WALLPAPER_KEY, 0))
}