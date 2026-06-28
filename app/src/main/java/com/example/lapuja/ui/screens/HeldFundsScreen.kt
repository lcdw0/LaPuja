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
import com.example.lapuja.data.remote.SaldoRetenidoItemResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HeldFundsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var totalRetenido by remember { mutableStateOf(0.0) }
    var items by remember { mutableStateOf<List<SaldoRetenidoItemResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarRetenido() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val response = RetrofitClient.api.obtenerSaldoRetenido(usuarioId)

                if (response.isSuccessful && response.body()?.ok == true) {
                    totalRetenido = response.body()?.totalRetenido ?: 0.0
                    items = response.body()?.items ?: emptyList()
                } else {
                    mensaje = response.body()?.let { "No se pudo cargar el saldo retenido." }
                        ?: "No se pudo cargar el saldo retenido."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarRetenido()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text("Saldo retenido", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dinero retenido en subastas donde vas ganando.",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total retenido", fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$${String.format("%.2f", totalRetenido)}",
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (cargando) {
            item {
                Text("Cargando saldo retenido...", color = Color.Gray)
            }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
        }

        if (!cargando && mensaje.isEmpty() && items.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No tienes saldo retenido", fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cuando vayas ganando una subasta, el monto retenido aparecerá aquí.",
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(items) { item ->
            HeldFundCard(item = item)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HeldFundCard(
    item: SaldoRetenidoItemResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = item.nombreSubasta ?: "Subasta",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Monto retenido: $${String.format("%.2f", item.monto ?: 0.0)}",
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Estado: ${item.estado ?: "No disponible"}",
                color = Color.Gray
            )

            Text(
                text = "Finaliza: ${formatearFechaRetenido(item.fechaFin)}",
                color = Color.Gray
            )
        }
    }
}

private fun formatearFechaRetenido(fecha: String?): String {
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