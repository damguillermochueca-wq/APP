rootProject.name = "Nexus11_v2"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()            // Sin filtros, para que encuentre todo lo de Android
        mavenCentral()      // Esencial para los plugins de Kotlin y Compose
        gradlePluginPortal()
    }
}

plugins {
    // ✅ Esto soluciona tus errores de Java 17 (descarga el JDK si falta)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()      // ✅ AQUÍ es donde viven Voyager, Kamel y Ktor
    }
}

include(":composeApp")