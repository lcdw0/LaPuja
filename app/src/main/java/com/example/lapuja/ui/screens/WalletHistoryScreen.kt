package com.example.lapuja.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.WalletMovimientoResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletHistoryScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var movimientos by remember { mutableStateOf<List<WalletMovimientoResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarMovimientos() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val response = RetrofitClient.api.listarMovimientosWallet(usuarioId)

                if (response.isSuccessful) {
                    movimientos = response.body() ?: emptyList()
                } else {
                    mensaje = "No se pudo cargar el historial."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarMovimientos()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text("Historial Wallet", fontSize = 30.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Movimientos de saldo de tu cuenta.", color = Color.Gray)
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (cargando) {
            item { Text("Cargando movimientos...", color = Color.Gray) }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
        }

        if (!cargando && mensaje.isEmpty() && movimientos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No hay movimientos", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cuando recargues o uses tu saldo, aparecerá aquí.", color = Color.Gray)
                    }
                }
            }
        }

        items(movimientos) { movimiento ->
            WalletMovementCard(movimiento)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WalletMovementCard(
    movimiento: WalletMovimientoResponse
) {
    val tipo = movimiento.tipo ?: "MOVIMIENTO"
    val monto = movimiento.monto ?: 0.0
    val descripcion = movimiento.descripcion ?: "Sin descripción"
    val fecha = formatearFechaWallet(movimiento.fecha)

    val esIngreso = tipo == "RECARGA" || tipo == "VENTA" || tipo == "REEMBOLSO"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = iconoMovimiento(tipo) + " " + descripcion,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fecha,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Text(
                text = if (esIngreso) "+$${String.format("%.2f", monto)}"
                else "-$${String.format("%.2f", monto)}",
                color = if (esIngreso) Color(0xFF4CAF50) else Color(0xFFE84D5B),
                fontSize = 18.sp
            )
        }
    }
}

private fun iconoMovimiento(tipo: String): String {
    return when (tipo.uppercase()) {
        "RECARGA" -> "➕"
        "PUJA" -> "🔥"
        "VENTA" -> "💰"
        "REEMBOLSO" -> "↩️"
        else -> "💳"
    }
}

private fun formatearFechaWallet(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "No disponible"

    return try {
        val limpia = fecha.substringBefore(".")
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val salida = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault())
        val date = entrada.parse(limpia)
        salida.format(date!!)
    } catch (e: Exception) {
        fecha
    }
}