import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // ✅ Cambiamos a 2.0.21 que es la versión estable más robusta para evitar errores de caché en iOS
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.nexus11"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nexus11"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/kotlinx-serialization-json.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ✅ ESTO OBLIGA A TODO EL PROYECTO A USAR UNA SOLA VERSIÓN DE KTOR
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.ktor") {
                useVersion("2.3.12")
            }
        }
    }
}

kotlin {
    // ✅ Alineamos la versión del target con el plugin
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // 🖼️ IMÁGENES (Solo Galería)
            implementation("media.kamel:kamel-image:1.0.3")
            implementation("io.github.onseok:peekaboo-ui:0.5.2")
            implementation("io.github.onseok:peekaboo-image-picker:0.5.2")

            // 🌐 KTOR (Paquete unificado para iOS/Android)
            val ktorVersion = "2.3.12"
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("io.ktor:ktor-client-auth:$ktorVersion")
            implementation("io.ktor:ktor-client-encoding:$ktorVersion")

            implementation("com.russhwolf:multiplatform-settings-no-arg:1.2.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // ✅ Motor estable para Android
            implementation("io.ktor:ktor-client-okhttp:2.3.12")
        }

        iosMain.dependencies {
            // ✅ Motor nativo para iOS
            implementation("io.ktor:ktor-client-darwin:2.3.12")
        }
    }
}