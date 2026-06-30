package com.example.lapuja.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.remote.MetodoPagoRequest
import com.example.lapuja.data.remote.MetodoPagoResponse
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var metodos by remember { mutableStateOf<List<MetodoPagoResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }

    fun cargarMetodos() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                if (usuarioId == 0L) {
                    mensaje = "Debes iniciar sesión para ver tus métodos de pago."
                    return@launch
                }

                val response = RetrofitClient.api.listarMetodosPago(usuarioId)

                if (response.isSuccessful) {
                    metodos = response.body() ?: emptyList()
                } else {
                    mensaje = "No se pudieron cargar los métodos de pago."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarMetodos()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text("Métodos de Pago", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Administra tus tarjetas guardadas.",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (cargando) {
            item {
                Text("Cargando métodos de pago...", color = Color.Gray)
            }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Text(
                    text = mensaje,
                    color = if (
                        mensaje.contains("correctamente") ||
                        mensaje.contains("eliminado")
                    ) {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (!cargando && mensaje.isEmpty() && metodos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No tienes métodos de pago", fontSize = 21.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Agrega una tarjeta para usarla en tus compras y recargas.",
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        items(metodos) { metodo ->
            PaymentCard(
                metodo = metodo,
                onDelete = {
                    scope.launch {
                        try {
                            val id = metodo.id ?: return@launch

                            val response = RetrofitClient.api.eliminarMetodoPago(id)

                            if (response.isSuccessful && response.body()?.ok == true) {
                                mensaje = "Método eliminado correctamente."
                                cargarMetodos()
                            } else {
                                mensaje = response.body()?.mensaje ?: "No se pudo eliminar."
                            }
                        } catch (e: Exception) {
                            mensaje = "No se pudo conectar con la API."
                        }
                    }
                },
                onMakePrincipal = {
                    scope.launch {
                        try {
                            val id = metodo.id ?: return@launch

                            val response = RetrofitClient.api.marcarMetodoPrincipal(id)

                            if (response.isSuccessful && response.body()?.ok == true) {
                                mensaje = "Tarjeta principal actualizada."
                                cargarMetodos()
                            } else {
                                mensaje = response.body()?.mensaje
                                    ?: "No se pudo actualizar."
                            }
                        } catch (e: Exception) {
                            mensaje = "No se pudo conectar con la API."
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    mostrarFormulario = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Agregar método")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (mostrarFormulario) {
        AddPaymentMethodDialog(
            usuarioId = usuarioId,
            onDismiss = {
                mostrarFormulario = false
            },
            onMetodoAgregado = {
                mostrarFormulario = false
                mensaje = "Método de pago agregado correctamente."
                cargarMetodos()
            },
            onError = {
                mensaje = it
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodDialog(
    usuarioId: Long,
    onDismiss: () -> Unit,
    onMetodoAgregado: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var titular by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("VISA") }
    var vencimiento by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf("") }
    var marcaExpandida by remember { mutableStateOf(false) }

    val marcas = listOf("VISA", "MASTERCARD", "AMEX")
    val longitudEsperada = if (marca == "AMEX") 15 else 16

    AlertDialog(
        onDismissRequest = {
            if (!cargando) onDismiss()
        },
        title = {
            Text("Agregar tarjeta")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titular,
                    onValueChange = {
                        titular = it
                        errorLocal = ""
                    },
                    label = { Text("Titular de la tarjeta") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = marcaExpandida,
                    onExpandedChange = {
                        marcaExpandida = !marcaExpandida
                    }
                ) {
                    OutlinedTextField(
                        value = marca,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de tarjeta") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = marcaExpandida
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = marcaExpandida,
                        onDismissRequest = { marcaExpandida = false }
                    ) {
                        marcas.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    marca = opcion
                                    marcaExpandida = false
                                    numeroTarjeta = numeroTarjeta.take(
                                        if (opcion == "AMEX") 15 else 16
                                    )
                                    errorLocal = ""
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = numeroTarjeta,
                    onValueChange = {
                        numeroTarjeta = it
                            .filter { c -> c.isDigit() }
                            .take(longitudEsperada)

                        errorLocal = ""
                    },
                    label = { Text("Número de tarjeta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        Text(
                            if (marca == "AMEX")
                                "AMEX usa 15 dígitos"
                            else
                                "$marca usa 16 dígitos"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vencimiento,
                    onValueChange = {
                        vencimiento = formatearVencimientoTarjeta(it)
                        errorLocal = ""
                    },
                    label = { Text("Vencimiento MM/AA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Establecer como principal")
                    Switch(
                        checked = principal,
                        onCheckedChange = { principal = it }
                    )
                }

                if (errorLocal.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorLocal,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando,
                onClick = {
                    when {
                        usuarioId == 0L -> {
                            errorLocal = "Debes iniciar sesión."
                            return@Button
                        }

                        titular.isBlank() -> {
                            errorLocal = "Ingresa el titular de la tarjeta."
                            return@Button
                        }

                        numeroTarjeta.length != longitudEsperada -> {
                            errorLocal = "El número de $marca debe tener $longitudEsperada dígitos."
                            return@Button
                        }

                        !vencimiento.matches(Regex("""\d{2}/\d{2}""")) -> {
                            errorLocal = "El vencimiento debe tener formato MM/AA."
                            return@Button
                        }

                        !vencimientoEsValido(vencimiento) -> {
                            errorLocal = "La fecha de vencimiento no es válida."
                            return@Button
                        }
                    }

                    val ultimos4 = numeroTarjeta.takeLast(4)

                    cargando = true

                    scope.launch {
                        try {
                            val response = RetrofitClient.api.agregarMetodoPago(
                                MetodoPagoRequest(
                                    usuarioId = usuarioId,
                                    tipo = "TARJETA",
                                    marca = marca,
                                    titular = titular.trim(),
                                    ultimos4 = ultimos4,
                                    vencimiento = vencimiento,
                                    principal = principal
                                )
                            )

                            if (response.isSuccessful && response.body()?.ok == true) {
                                onMetodoAgregado()
                            } else {
                                val msg = response.body()?.mensaje
                                    ?: "No se pudo agregar el método de pago."
                                errorLocal = msg
                                onError(msg)
                            }
                        } catch (e: Exception) {
                            errorLocal = "No se pudo conectar con la API."
                            onError("No se pudo conectar con la API.")
                        } finally {
                            cargando = false
                        }
                    }
                }
            ) {
                Text(if (cargando) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !cargando,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PaymentCard(
    metodo: MetodoPagoResponse,
    onDelete: () -> Unit,
    onMakePrincipal: () -> Unit
) {
    val marca = metodo.marca ?: "TARJETA"
    val ultimos4 = metodo.ultimos4 ?: "0000"
    val titular = metodo.titular ?: "Sin titular"
    val vencimiento = metodo.vencimiento ?: "--/--"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (metodo.principal == true) 205.dp else 255.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2B2D42),
                            Color(0xFFFF6B81)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = iconoMarca(marca) + " " + marca.uppercase(),
                        color = Color.White,
                        fontSize = 20.sp
                    )

                    if (metodo.principal == true) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "Principal",
                                color = Color.White,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "**** **** **** $ultimos4",
                    color = Color.White,
                    fontSize = 26.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Titular",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Text(
                            text = titular,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Vence",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Text(
                            text = vencimiento,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }

                if (metodo.principal != true) {
                    OutlinedButton(
                        onClick = onMakePrincipal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hacer principal")
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar tarjeta", color = Color.White)
                }
            }
        }
    }
}

private fun iconoMarca(marca: String): String {
    return when (marca.uppercase()) {
        "VISA" -> "💳"
        "MASTERCARD" -> "💳"
        "AMEX", "AMERICAN EXPRESS" -> "💳"
        else -> "💳"
    }
}

private fun formatearVencimientoTarjeta(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)

    return when {
        digits.length <= 2 -> digits
        else -> digits.substring(0, 2) + "/" + digits.substring(2)
    }
}

private fun vencimientoEsValido(vencimiento: String): Boolean {
    val partes = vencimiento.split("/")

    if (partes.size != 2) return false

    val mes = partes[0].toIntOrNull() ?: return false
    val anioDosDigitos = partes[1].toIntOrNull() ?: return false

    if (mes !in 1..12) return false

    val calendario = Calendar.getInstance()
    val anioActual = calendario.get(Calendar.YEAR) % 100
    val mesActual = calendario.get(Calendar.MONTH) + 1

    return when {
        anioDosDigitos > anioActual -> true
        anioDosDigitos == anioActual && mes >= mesActual -> true
        else -> false
    }
}