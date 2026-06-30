package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    var enviado by remember { mutableStateOf(false) }

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
            text = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            enabled = !cargando && !enviado
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else {
            Button(
                enabled = !enviado,
                onClick = {
                    if (correo.isBlank()) {
                        mensaje = "El correo es obligatorio"
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
                                        "Correo de recuperación enviado correctamente"
                                    } else {
                                        "No se pudo enviar el correo de recuperación"
                                    }

                                enviado = ok
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
                Text("Enviar enlace")
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