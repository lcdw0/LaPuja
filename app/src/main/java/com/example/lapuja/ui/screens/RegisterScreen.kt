package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.UsuarioRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    prefs: SharedPreferences
) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(85.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📝",
                                    fontSize = 38.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Crear cuenta",
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Registrate para publicar y participar en subastas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                mensaje = ""
                            },
                            label = {
                                Text("Nombre completo")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it
                                mensaje = ""
                            },
                            label = {
                                Text("Correo")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                telefono = it
                                mensaje = ""
                            },
                            label = {
                                Text("Teléfono")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = ciudad,
                            onValueChange = {
                                ciudad = it
                                mensaje = ""
                            },
                            label = {
                                Text("Ciudad")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                mensaje = ""
                            },
                            label = {
                                Text("Contraseña")
                            },
                            visualTransformation =
                                if (mostrarPassword) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        mostrarPassword = !mostrarPassword
                                    }
                                ) {
                                    Text(
                                        if (mostrarPassword) "Ocultar" else "Ver"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmarPassword,
                            onValueChange = {
                                confirmarPassword = it
                                mensaje = ""
                            },
                            label = {
                                Text("Confirmar contraseña")
                            },
                            visualTransformation =
                                if (mostrarPassword) VisualTransformation.None
                                else PasswordVisualTransformation(),
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

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = {
                                if (
                                    nombre.isBlank() ||
                                    correo.isBlank() ||
                                    telefono.isBlank() ||
                                    ciudad.isBlank() ||
                                    password.isBlank() ||
                                    confirmarPassword.isBlank()
                                ) {
                                    mensaje = "Completá todos los campos"
                                    return@Button
                                }

                                if (!correo.contains("@")) {
                                    mensaje = "Ingresá un correo válido"
                                    return@Button
                                }

                                if (password != confirmarPassword) {
                                    mensaje = "Las contraseñas no coinciden"
                                    return@Button
                                }

                                cargando = true
                                mensaje = ""

                                scope.launch {
                                    try {
                                        val response =
                                            RetrofitClient.api.registrarUsuario(
                                                UsuarioRequest(
                                                    nombre = nombre,
                                                    correo = correo,
                                                    password = password,
                                                    telefono = telefono,
                                                    ciudad = ciudad
                                                )
                                            )

                                        if (response.isSuccessful) {
                                            val body = response.body()

                                            if (body != null && body.ok) {
                                                navController.navigate("login") {
                                                    popUpTo("register") {
                                                        inclusive = true
                                                    }
                                                }
                                            } else {
                                                mensaje = body?.mensaje ?: "No se pudo registrar"
                                            }
                                        } else {
                                            mensaje = "Error del servidor"
                                        }
                                    } catch (e: Exception) {
                                        mensaje = "No se pudo conectar con la API"
                                    } finally {
                                        cargando = false
                                    }
                                }
                            },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (cargando)
                                    "Registrando..."
                                else
                                    "Registrarme"
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                navController.navigate("login")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Ya tengo cuenta")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}