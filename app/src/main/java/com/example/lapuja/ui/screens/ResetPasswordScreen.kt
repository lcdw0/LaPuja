package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.ResetPasswordRequest
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    correoInicial: String = ""
) {
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf(correoInicial) }
    var codigo by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Restablecer contraseña",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa el código que recibiste en tu correo.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            label = { Text("Correo") },
            singleLine = true,
            enabled = !cargando && !ok,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = codigo,
            onValueChange = {
                codigo = it.filter { c -> c.isDigit() }.take(6)
                mensaje = ""
            },
            label = { Text("Código de recuperación") },
            singleLine = true,
            enabled = !cargando && !ok,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nuevaPassword,
            onValueChange = {
                nuevaPassword = it
                mensaje = ""
            },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !cargando && !ok,
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
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !cargando && !ok,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else {
            Button(
                enabled = !ok,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    when {
                        correo.isBlank() -> {
                            mensaje = "El correo es obligatorio"
                        }

                        codigo.isBlank() -> {
                            mensaje = "El código es obligatorio"
                        }

                        codigo.length != 6 -> {
                            mensaje = "El código debe tener 6 dígitos"
                        }

                        nuevaPassword.isBlank() -> {
                            mensaje = "La nueva contraseña es obligatoria"
                        }

                        confirmarPassword.isBlank() -> {
                            mensaje = "Debes confirmar la contraseña"
                        }

                        nuevaPassword != confirmarPassword -> {
                            mensaje = "Las contraseñas no coinciden"
                        }

                        nuevaPassword.length < 8 -> {
                            mensaje = "La contraseña debe tener al menos 8 caracteres"
                        }

                        else -> {
                            scope.launch {
                                cargando = true
                                mensaje = ""

                                try {
                                    val response = RetrofitClient.api.restablecerPassword(
                                        ResetPasswordRequest(
                                            correo = correo,
                                            codigo = codigo,
                                            nuevaPassword = nuevaPassword,
                                            confirmarPassword = confirmarPassword
                                        )
                                    )

                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        ok = body?.get("ok") == true
                                        mensaje = body?.get("mensaje")?.toString()
                                            ?: if (ok) {
                                                "Contraseña restablecida correctamente"
                                            } else {
                                                "No se pudo restablecer la contraseña"
                                            }
                                    } else {
                                        mensaje = "Error al restablecer la contraseña"
                                    }
                                } catch (e: Exception) {
                                    mensaje = "No se pudo conectar con el servidor"
                                } finally {
                                    cargando = false
                                }
                            }
                        }
                    }
                }
            ) {
                Text("Restablecer contraseña")
            }
        }

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (ok) {
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Ir al login")
            }
        }
    }
}