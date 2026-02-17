This is a Kotlin Multiplatform project targeting Android, iOS.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

---

# Guía de Usuario: Nexus 11

**Credenciales de Acceso para Evaluación:**

* **Email:** `profe@gmail.com`
* **Contraseña:** `123456`

---

## 1. Inicio de Sesión

Al abrir la aplicación, encontrarás la pantalla de bienvenida.

1. Introduce las credenciales indicadas arriba.
2. Pulsa **"ENTRAR"**.
3. *Nota:* La aplicación recordará tu sesión. Si cierras la app y vuelves a abrirla, entrarás automáticamente al Feed principal.

## 2. Pantalla Principal (Feed)

Aquí verás la actividad de la red social. Tienes 3 pestañas superiores para filtrar el contenido:

* **Descubrir:** Muestra todas las publicaciones de la red en tiempo real.
* **Siguiendo:** Solo verás los posts de las personas a las que sigues.
* **Para ti:** Un filtro inteligente que te sugiere contenido basado en coincidencias de tu perfil (mismo oficio o hobbies).

**Interacciones:**

* **Like:** Toca el corazón ❤️ para dar "Me gusta" (verás el contador subir).
* **Comentar:** Escribe en la barra inferior de cada tarjeta y pulsa el avión de papel ✈️ para comentar.
* **Actualizar:** Desliza el dedo hacia abajo (*pull-to-refresh*) para cargar nuevos posts.

## 3. Crear una Publicación

Pulsa el botón **"+" (Crear)** en la barra inferior.

1. **Escribe:** Cuéntanos qué piensas (máximo 280 caracteres). El círculo de progreso te avisará si te excedes.
2. **Añade Foto:** Toca el icono de imagen para abrir la galería de tu móvil y selecciona una foto.
3. **Publicar:** Pulsa el botón superior derecho. Tu post aparecerá inmediatamente en el Feed.

## 4. Perfil de Usuario

Ve a la pestaña **"Perfil"** (icono de persona).

* **Tu información:** Verás tus estadísticas (Posts, Seguidores, Siguiendo).
* **Editar Foto:** Toca tu foto de perfil para cambiarla por una de tu galería.
* **Editar Datos:** Pulsa "Editar Perfil" para cambiar tu Biografía, Profesión, Hobby o Estado (Vibe). Esto generará etiquetas de colores en tu perfil.
* **Tus Posts:** Abajo verás tus fotos y textos organizados.

**Buscador:** Pulsa la lupa 🔍 arriba a la derecha para buscar a otros usuarios (ej. busca "alumno"). Al entrar en su perfil podrás:

* **Seguir/Dejar de Seguir:** Botón azul grande.
* **Mensaje:** Botón gris para ir al chat privado.

## 5. Chats y Mensajería

Ve a la pestaña **"Chats"** (icono de bocadillo).

* Verás tu lista de conversaciones activas con un punto azul si hay mensajes nuevos.
* **Dentro del chat:**
* Escribe mensajes de texto o envía fotos.
* **Funciones Avanzadas:**
* **Responder:** Desliza un mensaje hacia la derecha.
* **Editar/Borrar:** Mantén pulsado un mensaje tuyo para ver el menú de opciones (Editar, Eliminar, Copiar).
* **Reaccionar:** Mantén pulsado cualquier mensaje para añadir una reacción rápida (❤️ 😂 😮 👍).

## 6. Personalización

Esta es la función estrella. Desde tu **Perfil**, pulsa el icono de engranaje ⚙️ (Ajustes).

1. Entra en **"Apariencia"**.
2. **Color de Acento:** Elige cualquier color (Naranja, Rosa, Verde...). Verás que **toda la aplicación cambia de color al instante**.
3. **Fondo de Chat:** Elige entre "Oscuro", "Océano" o "Galaxia" para tus conversaciones.

## 7. Cerrar Sesión

Si deseas salir, ve a **Perfil > Ajustes (⚙️)**, baja hasta el final y pulsa el botón rojo **"Cerrar Sesión"**. Esto borrará tus credenciales del dispositivo de forma segura.