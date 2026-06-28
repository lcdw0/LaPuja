package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.Bid
import com.example.lapuja.data.remote.FavoritoRequest
import com.example.lapuja.data.remote.PujaRequest
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AuctionScreen(
    prefs: SharedPreferences,
    historial: MutableList<Bid>,
    productos: MutableList<AuctionItem>,
    onAuctionClick: (AuctionItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val usuarioId = prefs.getLong("usuarioId", 0L)

    val subastas = remember { mutableStateListOf<SubastaResponse>() }
    var favoritosIds by remember { mutableStateOf(setOf<Long>()) }
    var favoritosMap by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }

    val saldo by remember {
        mutableStateOf(prefs.getFloat("saldo", 10000.0f).toDouble())
    }

    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }
    var mensaje by remember { mutableStateOf("") }

    val filtros = listOf(
        "Todas",
        "Tecnología",
        "Computadoras",
        "Celulares",
        "Videojuegos",
        "Electrodomésticos",
        "Vehículos",
        "Ropa",
        "Hogar",
        "Coleccionables",
        "Otros",
        "Activas",
        "Finalizadas",
        "Guardadas"
    )

    fun cargarDatos() {
        scope.launch {
            try {
                val responseSubastas = RetrofitClient.api.listarSubastas()

                if (responseSubastas.isSuccessful) {
                    subastas.clear()
                    subastas.addAll(responseSubastas.body() ?: emptyList())
                }

                if (usuarioId != 0L) {
                    val responseFavoritos = RetrofitClient.api.listarFavoritos(usuarioId)

                    if (responseFavoritos.isSuccessful) {
                        val favoritos = responseFavoritos.body() ?: emptyList()

                        favoritosIds = favoritos.mapNotNull { it.subastaId }.toSet()

                        favoritosMap = favoritos
                            .filter { it.subastaId != null && it.id != null }
                            .associate { it.subastaId!! to it.id!! }
                    }
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            cargarDatos()
        }
    }

    val subastasFiltradas = subastas.filter { subasta ->
        val coincideBusqueda =
            subasta.nombre.contains(busqueda, ignoreCase = true) ||
                    subasta.categoria.contains(busqueda, ignoreCase = true)

        val estaGuardada = favoritosIds.contains(subasta.id)

        val coincideFiltro =
            when (filtroSeleccionado) {
                "Todas" -> true
                "Guardadas" -> estaGuardada
                "Activas" -> subasta.estado == "ACTIVA"
                "Finalizadas" -> subasta.estado == "FINALIZADA"
                else -> subasta.categoria == filtroSeleccionado
            }

        coincideBusqueda && coincideFiltro
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Subastas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                filtros.forEach { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = {
                            filtroSeleccionado = filtro
                        },
                        label = {
                            Text(filtro)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (subastasFiltradas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "No hay subastas para mostrar",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Probá con otra búsqueda o cambiá el filtro.",
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        items(subastasFiltradas) { subasta ->

            val guardada = favoritosIds.contains(subasta.id)
            val tiempoRestante = calcularTiempoRestante(subasta.fechaFin)

            val puedePujar =
                subasta.estado == "ACTIVA" &&
                        tiempoRestante != "Finalizada"

            val estadoColor =
                when (subasta.estado) {
                    "ACTIVA" -> Color(0xFF4CAF50)
                    "PROGRAMADA" -> Color(0xFFFFC107)
                    else -> Color(0xFFFF6B81)
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable {
                        onAuctionClick(subasta.toAuctionItem())
                    },
                shape = RoundedCornerShape(22.dp)
            ) {
                Column {
                    ImagenSubastaLista(
                        imagen = subasta.imagen,
                        nombre = subasta.nombre
                    )

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = subasta.nombre,
                                    fontSize = 24.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = subasta.categoria,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = estadoColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = subasta.estado,
                                    color = estadoColor,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 7.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Precio actual",
                            color = Color.Gray
                        )

                        Text(
                            text = "$${subasta.precioActual}",
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Ofertas realizadas: ${subasta.ofertas}")
                        Text("Ganador actual: ${subasta.ganador}")
                        Text("Tiempo restante: $tiempoRestante")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Saldo disponible: $$saldo",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (usuarioId == 0L) {
                                        mensaje = "Debe iniciar sesión para guardar."
                                        return@OutlinedButton
                                    }

                                    scope.launch {
                                        try {
                                            if (guardada) {
                                                val favoritoId = favoritosMap[subasta.id]

                                                if (favoritoId != null) {
                                                    RetrofitClient.api.eliminarFavorito(favoritoId)
                                                }
                                            } else {
                                                RetrofitClient.api.agregarFavorito(
                                                    FavoritoRequest(
                                                        usuarioId = usuarioId,
                                                        subastaId = subasta.id
                                                    )
                                                )
                                            }

                                            cargarDatos()
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo actualizar favoritos."
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    if (guardada) "❤️ Guardada" else "🤍 Guardar"
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (usuarioId == 0L) {
                                        mensaje = "Debe iniciar sesión para pujar."
                                        return@Button
                                    }

                                    val nuevaOferta = subasta.precioActual + 0.5

                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.api.crearPuja(
                                                PujaRequest(
                                                    usuarioId = usuarioId,
                                                    subastaId = subasta.id,
                                                    monto = nuevaOferta
                                                )
                                            )

                                            if (response.isSuccessful) {
                                                historial.add(
                                                    Bid(
                                                        producto = subasta.nombre,
                                                        precio = nuevaOferta,
                                                        ganador = "Tú"
                                                    )
                                                )

                                                cargarDatos()
                                            } else {
                                                mensaje = "No se pudo realizar la puja."
                                            }
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo conectar con la API."
                                        }
                                    }
                                },
                                enabled = puedePujar,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    if (puedePujar) "🔥 Pujar" else "No disponible"
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = subasta.ganador == prefs.getString("nombre", "")
                                    && subasta.estado == "ACTIVA"
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Vas ganando esta subasta",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        if (subasta.estado == "FINALIZADA" || tiempoRestante == "Finalizada") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Subasta terminada",
                                fontSize = 18.sp
                            )

                            Text(
                                text = "Ganador final: ${subasta.ganador}",
                                fontSize = 16.sp
                            )
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

@Composable
fun ImagenSubastaLista(
    imagen: String?,
    nombre: String
) {
    AppImage(
        imageUrl = imagen,
        contentDescription = nombre,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

private fun SubastaResponse.toAuctionItem(): AuctionItem {
    return AuctionItem(
        nombre = nombre,
        descripcion = descripcion,
        precioInicial = precioActual,
        imagen = 0,
        categoria = categoria,
        fechaInicio = fechaCreacion ?: "Hoy"
    ).apply {
        idApi = id
        precio = precioActual
        estado = estado
        ofertas = ofertas
        ganador = ganador
        iniciada = estado == "ACTIVA"
        tiempo = 0
    }
}

private fun calcularTiempoRestante(fechaFin: String?): String {
    if (fechaFin.isNullOrBlank()) return "No disponible"

    return try {
        val fechaLimpia = fechaFin.substringBefore(".")
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val fin = formato.parse(fechaLimpia)?.time ?: return "No disponible"

        val diferencia = fin - System.currentTimeMillis()

        if (diferencia <= 0) {
            "Finalizada"
        } else {
            val dias = TimeUnit.MILLISECONDS.toDays(diferencia)
            val horas = TimeUnit.MILLISECONDS.toHours(diferencia) % 24
            val minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia) % 60

            when {
                dias > 0 -> "${dias}d ${horas}h"
                horas > 0 -> "${horas}h ${minutos}m"
                else -> "${minutos}m"
            }
        }
    } catch (e: Exception) {
        "No disponible"
    }
}