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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.launch

@Composable
fun MyAuctionsScreen(
    productos: MutableList<AuctionItem>
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var misSubastas by remember { mutableStateOf<List<SubastaResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarMisSubastas() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                if (usuarioId == 0L) {
                    mensaje = "Debes iniciar sesión para ver tus subastas."
                    misSubastas = emptyList()
                    return@launch
                }

                val response = RetrofitClient.api.listarSubastasPorUsuario(usuarioId)

                if (response.isSuccessful) {
                    misSubastas = response.body() ?: emptyList()
                } else {
                    mensaje = "No se pudieron cargar tus subastas."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarMisSubastas()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(text = "Mis subastas", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gestiona las subastas que has publicado.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (cargando) {
            item {
                Text(
                    text = "Cargando tus subastas...",
                    color = Color.Gray
                )
            }
        }

        if (mensaje.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        if (!cargando && mensaje.isEmpty() && misSubastas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "No has publicado subastas",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Las subastas que crees aparecerán aquí.",
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(misSubastas.reversed()) { auction ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    AppImage(
                        imageUrl = auction.imagen,
                        contentDescription = auction.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = auction.nombre,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Categoría: ${auction.categoria}")
                        Text(text = "Precio actual: $${auction.precioActual}")
                        Text(text = "Ofertas: ${auction.ofertas}")
                        Text(text = "Ganador: ${auction.ganador}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Estado: ${auction.estado}",
                            color = when (auction.estado) {
                                "ACTIVA" -> Color(0xFF4CAF50)
                                "PROGRAMADA" -> Color(0xFFFFC107)
                                else -> Color(0xFFFF6B81)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (auction.estado == "ACTIVA") {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val response =
                                                RetrofitClient.api.finalizarSubasta(auction.id)

                                            if (response.isSuccessful) {
                                                cargarMisSubastas()
                                            } else {
                                                mensaje = "No se pudo finalizar la subasta."
                                            }
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo conectar con la API."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Finalizar subasta")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}