package com.example.nexus11.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.nexus11.data.AuthRepository
import com.example.nexus11.ui.screens.MainScreen
import com.example.nexus11.ui.theme.*
import kotlinx.coroutines.launch

// ✅ CAMBIO 1: Convertido a clase Screen de Voyager
class LoginScreen : Screen {

    @Composable
    override fun Content() {
        // ✅ CAMBIO 2: Obtenemos el navigator aquí
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val authRepository = remember { AuthRepository() }

        // --- ESTADOS ---
        var isRegister by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // FONDO NEGRO PURO (OLED)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NexusBlack),
            contentAlignment = Alignment.Center
        ) {
            // TARJETA CENTRAL
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NexusDarkGray),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TÍTULO
                    Text(
                        text = if (isRegister) "Crear Cuenta" else "Bienvenido",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isRegister) "Únete a la comunidad Nexus" else "Te echábamos de menos",
                        color = TextGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    // MENSAJE DE ERROR
                    errorMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = msg, color = ErrorRed, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // CAMPO USUARIO (Solo en Registro)
                    AnimatedVisibility(visible = isRegister) {
                        Column {
                            NexusTextField(
                                value = username,
                                onValueChange = { username = it },
                                icon = Icons.Default.Person,
                                placeholder = "Usuario"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // EMAIL
                    NexusTextField(
                        value = email,
                        onValueChange = { email = it },
                        icon = Icons.Default.Email,
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD
                    NexusTextField(
                        value = password,
                        onValueChange = { password = it },
                        icon = Icons.Default.Lock,
                        placeholder = "Contraseña",
                        isPassword = true,
                        isVisible = passwordVisible,
                        onVisibilityChange = { passwordVisible = !passwordVisible },
                        keyboardType = KeyboardType.Password
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // BOTÓN DE ACCIÓN
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank() || (isRegister && username.isBlank())) {
                                errorMessage = "Por favor, rellena todos los campos."
                                return@Button
                            }

                            scope.launch {
                                try {
                                    errorMessage = null
                                    isLoading = true

                                    if (isRegister) {
                                        authRepository.signUp(email, password, username)
                                    } else {
                                        authRepository.login(email, password)
                                    }
                                    isLoading = false

                                    // ✅ CAMBIO 3: Navegación correcta al MainScreen
                                    navigator.replaceAll(MainScreen())

                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = e.message?.replace("Exception: ", "") ?: "Error desconocido"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusBlue,
                            disabledContainerColor = NexusBlue.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isRegister) "REGISTRARSE" else "ENTRAR",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SWITCH LOGIN <-> REGISTRO
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isRegister) "¿Ya tienes cuenta?" else "¿Nuevo en Nexus?",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRegister) "Entra aquí" else "Crea una",
                            color = NexusBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                isRegister = !isRegister
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE INPUT REUTILIZABLE ---
@Composable
fun NexusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    isPassword: Boolean = false,
    isVisible: Boolean = true,
    onVisibilityChange: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NexusBlue) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextGray
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NexusBlack,
            unfocusedContainerColor = NexusBlack,
            focusedBorderColor = NexusBlue,
            unfocusedBorderColor = Color(0xFF333333),
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = NexusBlue
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}