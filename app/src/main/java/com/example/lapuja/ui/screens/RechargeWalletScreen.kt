package com.example.lapuja.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.MetodoPagoResponse
import com.example.lapuja.data.remote.RecargaRequest
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RechargeWalletScreen(
    navController: NavController,
    prefs: SharedPreferences
) {
    val context = LocalContext.current
    val appPrefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = appPrefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var metodos by remember { mutableStateOf<List<MetodoPagoResponse>>(emptyList()) }
    var metodoSeleccionado by remember { mutableStateOf<MetodoPagoResponse?>(null) }
    var monto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    fun cargarMetodos() {
        scope.launch {
            try {
                val response = RetrofitClient.api.listarMetodosPago(usuarioId)
                if (response.isSuccessful) {
                    metodos = response.body() ?: emptyList()
                    metodoSeleccionado = metodos.firstOrNull { it.principal == true }
                        ?: metodos.firstOrNull()
                } else {
                    mensaje = "No se pudieron cargar tus tarjetas."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
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
            Text("Recargar Wallet", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona una tarjeta y el monto que deseas agregar.",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (metodos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No tienes tarjetas registradas", fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Agrega un método de pago antes de recargar tu Wallet.",
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                navController.navigate("payment_methods")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Agregar método de pago")
                        }
                    }
                }
            }

            return@LazyColumn
        }

        item {
            Text("Tarjeta seleccionada", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(metodos) { metodo ->
            RechargePaymentMethodCard(
                metodo = metodo,
                selected = metodoSeleccionado?.id == metodo.id,
                onClick = {
                    metodoSeleccionado = metodo
                    mensaje = ""
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text("Monto", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickAmountButton("100", Modifier.weight(1f)) { monto = "100" }
                Spacer(modifier = Modifier.width(8.dp))
                QuickAmountButton("250", Modifier.weight(1f)) { monto = "250" }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickAmountButton("500", Modifier.weight(1f)) { monto = "500" }
                Spacer(modifier = Modifier.width(8.dp))
                QuickAmountButton("1000", Modifier.weight(1f)) { monto = "1000" }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = monto,
                onValueChange = {
                    monto = it.filter { c -> c.isDigit() }.take(6)
                    mensaje = ""
                },
                label = { Text("Monto personalizado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensaje,
                    color = if (mensaje.contains("correctamente")) {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    val metodoId = metodoSeleccionado?.id

                    when {
                        usuarioId == 0L -> {
                            mensaje = "Debes iniciar sesión."
                            return@Button
                        }

                        metodoId == null -> {
                            mensaje = "Selecciona una tarjeta."
                            return@Button
                        }

                        montoDouble == null || montoDouble <= 0 -> {
                            mensaje = "Ingresa un monto válido."
                            return@Button
                        }
                    }

                    cargando = true
                    mensaje = ""

                    scope.launch {
                        try {
                            val response = RetrofitClient.api.recargarSaldo(
                                usuarioId = usuarioId,
                                request = RecargaRequest(
                                    metodoPagoId = metodoId,
                                    monto = montoDouble
                                )
                            )

                            if (response.isSuccessful && response.body()?.ok == true) {
                                val nuevoSaldo = response.body()?.saldo ?: 0.0

                                prefs.edit()
                                    .putFloat("saldo", nuevoSaldo.toFloat())
                                    .apply()

                                mensaje = "Saldo recargado correctamente."
                                monto = ""
                            } else {
                                mensaje = response.body()?.mensaje
                                    ?: "No se pudo recargar saldo."
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
                Text(if (cargando) "Recargando..." else "Recargar saldo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RechargePaymentMethodCard(
    metodo: MetodoPagoResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    val marca = metodo.marca ?: "TARJETA"
    val ultimos4 = metodo.ultimos4 ?: "0000"
    val titular = metodo.titular ?: "Sin titular"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💳 ${marca.uppercase()} ****$ultimos4",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = titular,
                color = Color.Gray
            )

            if (selected) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Seleccionada", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun QuickAmountButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("C$$text")
    }
}