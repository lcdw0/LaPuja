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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    prefs: SharedPreferences
) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("Nicaragua") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val paisesCiudades = mapOf(
        "Nicaragua" to listOf("Managua", "León", "Granada", "Masaya", "Chinandega", "Estelí", "Matagalpa"),
        "Costa Rica" to listOf("San José", "Alajuela", "Cartago", "Heredia", "Puntarenas", "Limón"),
        "Honduras" to listOf("Tegucigalpa", "San Pedro Sula", "La Ceiba", "Choloma", "Comayagua"),
        "El Salvador" to listOf("San Salvador", "Santa Ana", "San Miguel", "Soyapango"),
        "Guatemala" to listOf("Ciudad de Guatemala", "Quetzaltenango", "Escuintla", "Mixco"),
        "Panamá" to listOf("Ciudad de Panamá", "Colón", "David", "La Chorrera")
    )

    val ciudadesDisponibles = paisesCiudades[pais] ?: emptyList()

    LaunchedEffect(pais) {
        ciudad = ciudadesDisponibles.firstOrNull() ?: ""
    }


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
                            label = { Text("Nombres") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = apellidos,
                            onValueChange = {
                                apellidos = it
                                mensaje = ""
                            },
                            label = { Text("Apellidos") },
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
                                telefono = it.filter { char -> char.isDigit() || char == '-' || char == ' ' }
                                mensaje = ""
                            },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SimpleDropdownField(
                            label = "País",
                            value = pais,
                            options = paisesCiudades.keys.toList(),
                            onValueChange = {
                                pais = it
                                mensaje = ""
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SimpleDropdownField(
                            label = "Ciudad",
                            value = ciudad,
                            options = ciudadesDisponibles,
                            onValueChange = {
                                ciudad = it
                                mensaje = ""
                            }
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
                                    apellidos.isBlank() ||
                                    correo.isBlank() ||
                                    telefono.isBlank() ||
                                    pais.isBlank() ||
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

                                if (!telefonoValidoPorPais(telefono, pais)) {
                                    mensaje = "Ingresá un teléfono válido para $pais"
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
                                                    apellidos = apellidos,
                                                    correo = correo,
                                                    password = password,
                                                    telefono = telefono,
                                                    pais = pais,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun telefonoValidoPorPais(
    telefono: String,
    pais: String
): Boolean {
    val limpio = telefono.replace(" ", "").replace("-", "")

    return when (pais) {
        "Nicaragua", "Costa Rica", "Honduras", "El Salvador", "Panamá" ->
            limpio.length == 8 && limpio.all { it.isDigit() }

        "Guatemala" ->
            limpio.length == 8 && limpio.all { it.isDigit() }

        else ->
            limpio.length >= 7 && limpio.all { it.isDigit() }
    }
}