package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ingresa tu correo y te enviaremos un código de 6 dígitos.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    if (correo.isBlank()) {
                        mensaje = "El correo es obligatorio"
                        return@Button
                    }

                    if (!correo.contains("@")) {
                        mensaje = "Ingresa un correo válido"
                        return@Button
                    }

                    scope.launch {
                        cargando = true
                        mensaje = ""

                        try {
                            val response = RetrofitClient.api.solicitarRecuperacion(
                                mapOf("correo" to correo)
                            )

                            if (response.isSuccessful) {
                                val body = response.body()
                                val ok = body?.get("ok") == true

                                mensaje = body?.get("mensaje")?.toString()
                                    ?: if (ok) {
                                        "Código enviado correctamente"
                                    } else {
                                        "No se pudo enviar el código"
                                    }

                                if (ok) {
                                    navController.navigate("reset_password_codigo/${correo}")
                                }
                            } else {
                                mensaje = "Error al solicitar recuperación"
                            }
                        } catch (e: Exception) {
                            mensaje = "No se pudo conectar con el servidor"
                        } finally {
                            cargando = false
                        }
                    }
                }
            ) {
                Text("Enviar código")
            }
        }

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                navController.navigate("login") {
                    popUpTo("forgot_password") {
                        inclusive = true
                    }
                }
            }
        ) {
            Text("Volver al login")
        }
    }
}