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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.remote.CancelarSubastaRequest
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.launch

@Composable
fun MyAuctionsScreen(
    productos: MutableList<AuctionItem>,
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var misSubastas by remember { mutableStateOf<List<SubastaResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    var mostrarDialogoCancelar by remember { mutableStateOf(false) }
    var subastaSeleccionada by remember { mutableStateOf<SubastaResponse?>(null) }

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

    fun cancelarSubasta(subasta: SubastaResponse) {
        scope.launch {
            try {
                val response = RetrofitClient.api.cancelarSubasta(
                    id = subasta.id,
                    request = CancelarSubastaRequest(usuarioId = usuarioId)
                )

                if (response.isSuccessful) {
                    mensaje = "Subasta cancelada correctamente."
                    cargarMisSubastas()
                } else {
                    mensaje = "No se pudo cancelar la subasta."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
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
                            color = if (mensaje.contains("correctamente")) {
                                Color(0xFF4CAF50)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        if (!cargando && misSubastas.isEmpty()) {
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
            val puedeModificar = auction.estado == "ACTIVA" && auction.ofertas == 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clickable {
                        navController.navigate("auction_detail/${auction.id}")
                    },
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
                        Text(text = "Precio inicial: $${auction.precioInicial}")
                        Text(text = "Precio actual: $${auction.precioActual}")
                        Text(text = "Ofertas: ${auction.ofertas}")
                        Text(text = "Ganador: ${auction.ganador}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Estado: ${auction.estado}",
                            color = when (auction.estado) {
                                "ACTIVA" -> Color(0xFF4CAF50)
                                "CANCELADA" -> Color.Gray
                                else -> Color(0xFFFF6B81)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (puedeModificar) {
                            Button(
                                onClick = {
                                    navController.navigate("edit_auction/${auction.id}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Editar")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    subastaSeleccionada = auction
                                    mostrarDialogoCancelar = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Cancelar subasta")
                            }
                        }

                        if (auction.estado == "ACTIVA" && auction.ofertas > 0) {
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

    if (mostrarDialogoCancelar && subastaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCancelar = false
                subastaSeleccionada = null
            },
            title = {
                Text("Cancelar subasta")
            },
            text = {
                Text("¿Seguro que deseas cancelar esta subasta? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val subasta = subastaSeleccionada
                        mostrarDialogoCancelar = false
                        subastaSeleccionada = null

                        if (subasta != null) {
                            cancelarSubasta(subasta)
                        }
                    }
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCancelar = false
                        subastaSeleccionada = null
                    }
                ) {
                    Text("Volver")
                }
            }
        )
    }
}