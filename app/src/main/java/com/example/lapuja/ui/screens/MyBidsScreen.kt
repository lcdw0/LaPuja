package com.example.lapuja.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.Bid
import com.example.lapuja.data.remote.PujaResponse
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import kotlinx.coroutines.launch

@Composable
fun MyBidsScreen(
    historial: MutableList<Bid>,
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var misPujas by remember { mutableStateOf<List<PujaResponse>>(emptyList()) }
    var subastasMap by remember { mutableStateOf<Map<Long, SubastaResponse>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarMisPujas() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                if (usuarioId == 0L) {
                    mensaje = "Debes iniciar sesión para ver tus ofertas."
                    return@launch
                }

                val response = RetrofitClient.api.listarPujasPorUsuario(usuarioId)

                if (response.isSuccessful) {
                    val pujas = response.body() ?: emptyList()
                    misPujas = pujas

                    val mapaTemporal = mutableMapOf<Long, SubastaResponse>()

                    pujas.mapNotNull { it.subastaId }
                        .distinct()
                        .forEach { subastaId ->
                            val subastaResponse = RetrofitClient.api.obtenerSubasta(subastaId)

                            if (subastaResponse.isSuccessful) {
                                subastaResponse.body()?.let {
                                    mapaTemporal[subastaId] = it
                                }
                            }
                        }

                    subastasMap = mapaTemporal
                } else {
                    mensaje = "No se pudieron cargar tus ofertas."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarMisPujas()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Mis ofertas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Resumen de las subastas en las que has participado.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (cargando) {
            item {
                Text(
                    text = "Cargando tus ofertas...",
                    color = Color.Gray
                )
            }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (!cargando && mensaje.isEmpty() && misPujas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "No tienes ofertas todavía",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cuando pujes por un producto, aparecerá aquí.",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(misPujas.reversed()) { puja ->
            val subasta = puja.subastaId?.let { subastasMap[it] }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clickable {
                        puja.subastaId?.let { id ->
                            navController.navigate("auction_detail/$id")
                        }
                    },
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        ImagenSubastaLista(
                            imagen = subasta?.imagen,
                            nombre = subasta?.nombre ?: "Subasta"
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = subasta?.nombre ?: "Subasta #${puja.subastaId ?: "N/D"}",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Categoría: ${subasta?.categoria ?: "No disponible"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tu oferta: $${puja.monto ?: 0.0}",
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Precio actual: $${subasta?.precioActual ?: 0.0}",
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Estado: ${subasta?.estado ?: "No disponible"}",
                            fontSize = 14.sp,
                            color = when (subasta?.estado) {
                                "ACTIVA" -> Color(0xFF4CAF50)
                                "FINALIZADA" -> Color(0xFFFF6B81)
                                else -> Color.Gray
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Fecha: ${formatearFecha(puja.fecha)}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}