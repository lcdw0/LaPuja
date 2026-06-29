package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.LoginRequest
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    prefs: SharedPreferences
) {
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF121212),
                        Color(0xFF1E1E1E)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "💰", fontSize = 40.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(text = "LaPuja", fontSize = 34.sp)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Iniciá sesión para ofertar y ganar",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        mensaje = ""
                    },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensaje = ""
                    },
                    label = { Text("Contraseña") },
                    visualTransformation =
                        if (mostrarPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                mostrarPassword = !mostrarPassword
                            }
                        ) {
                            Text(if (mostrarPassword) "Ocultar" else "Ver")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = mensaje,
                        color = Color(0xFFFF6B81),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (correo.isBlank() || password.isBlank()) {
                            mensaje = "Completa correo y contraseña."
                            return@Button
                        }

                        cargando = true
                        mensaje = ""

                        scope.launch {
                            try {
                                val response = RetrofitClient.api.loginUsuario(
                                    LoginRequest(
                                        correo = correo,
                                        password = password
                                    )
                                )

                                if (response.isSuccessful) {
                                    val body = response.body()

                                    if (body != null && body.ok) {
                                        prefs.edit()
                                            .putLong("usuarioId", body.id ?: 0L)
                                            .putString("nombre", body.nombre ?: "")
                                            .putString("correo", body.correo ?: "")
                                            .putString("fotoPerfil", body.fotoPerfil ?: "")
                                            .putString("telefono", body.telefono ?: "")
                                            .putString("ciudad", body.ciudad ?: "")
                                            .putString("biografia", body.biografia ?: "")
                                            .putString("fechaRegistro", body.fechaRegistro ?: "")
                                            .apply()

                                        navController.navigate("home") {
                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    } else {
                                        mensaje = body?.mensaje ?: "No se pudo iniciar sesión."
                                    }
                                } else {
                                    mensaje = "Error del servidor."
                                }
                            } catch (e: Exception) {
                                mensaje = "No se pudo conectar con la API."
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (cargando) "Entrando..." else "Entrar")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate("register")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}