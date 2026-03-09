# 📱 Nexus 11 - Red Social Multiplataforma

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-000000?style=for-the-badge&logo=Jetpack+Compose&logoColor=white)

Nexus 11 es una aplicación móvil multiplataforma (iOS y Android) desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**. 

Diseñada como Proyecto Final de Ciclo (DAM), esta red social demuestra la implementación de arquitecturas modernas, compartiendo la lógica de negocio y una interfaz gráfica (UI) 100% reactiva y unificada desde un único código base.

## ✨ Funcionalidades Principales

### 🔄 Feed Dinámico y Social
* **Algoritmo de filtrado:** Pestañas de contenido global (Descubrir), red de contactos (Siguiendo) y sugerencias inteligentes (Para ti) basadas en afinidad de perfil.
* **Interacciones en tiempo real:** Sistema de *Likes* y comentarios integrados en cada publicación.
* **Publicaciones ricas:** Creación de posts con texto (límite de 280 caracteres con indicador visual) y carga de imágenes desde la galería del dispositivo.
* **Pull-to-refresh:** Carga bajo demanda de nuevo contenido.

### 💬 Mensajería Avanzada (Chats)
* **Conversaciones activas:** Bandeja de entrada con indicadores de mensajes no leídos.
* **Interacción fluida:** Soporte para enviar texto e imágenes.
* **Acciones de mensaje:** Gestos nativos como *swipe-to-reply* (deslizar para responder), además de edición, eliminación y reacciones con emojis (❤️ 😂 😮 👍) mediante pulsación larga.

### 🎨 Personalización y Estado Global
* **Theming Dinámico:** Modificación del "Color de Acento" que propaga el cambio de estado a toda la UI de la aplicación de forma instantánea.
* **Fondos personalizados:** Soporte para diferentes temas en las salas de chat (Oscuro, Océano, Galaxia).

### 👤 Gestión de Perfil
* Edición completa de biografía, avatar, profesión y *Vibe* mediante un sistema de etiquetas visuales.
* Buscador de usuarios integrado para explorar la red y gestionar seguimientos.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Kotlin
* **Core & UI:** Kotlin Multiplatform + Compose Multiplatform
* **Arquitectura:** Patrón de diseño orientado a la separación de responsabilidades y gestión reactiva del estado de la UI.

## 🚀 Cómo ejecutar el proyecto

Este proyecto contiene módulos compartidos (`/composeApp` con `commonMain`, `iosMain`, `jvmMain`) y el punto de entrada nativo para Apple (`/iosApp`).

### Compilar y ejecutar en Android
Puedes usar la configuración de ejecución de tu IDE (Android Studio / IntelliJ) o compilar desde la terminal:

**macOS/Linux:**
```bash
./gradlew :composeApp:assembleDebug
