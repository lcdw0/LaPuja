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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
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

    var saldo by remember {
        mutableStateOf(prefs.getFloat("saldo", 10000.0f).toDouble())
    }

    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var estadoSeleccionado by remember { mutableStateOf("TODAS") }
    var precioMinimo by remember { mutableStateOf("") }
    var precioMaximo by remember { mutableStateOf("") }
    var ordenSeleccionado by remember { mutableStateOf("recientes") }

    var mostrarFiltros by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val categorias = listOf(
        "Todas", "Tecnología", "Computadoras", "Celulares", "Videojuegos",
        "Electrodomésticos", "Vehículos", "Ropa", "Hogar", "Coleccionables", "Otros"
    )

    val estados = listOf(
        "TODAS" to "Todas",
        "ACTIVA" to "Activas",
        "FINALIZADA" to "Finalizadas"
    )

    val ordenes = listOf(
        "recientes" to "Más recientes",
        "precio_asc" to "Menor precio",
        "precio_desc" to "Mayor precio",
        "ofertas_desc" to "Más ofertadas",
        "fecha_fin_asc" to "Próximas a finalizar"
    )

    fun cargarFavoritosYSaldo() {
        scope.launch {
            try {
                if (usuarioId != 0L) {
                    val responseFavoritos = RetrofitClient.api.listarFavoritos(usuarioId)

                    if (responseFavoritos.isSuccessful) {
                        val favoritos = responseFavoritos.body() ?: emptyList()
                        favoritosIds = favoritos.mapNotNull { it.subastaId }.toSet()
                        favoritosMap = favoritos
                            .filter { it.subastaId != null && it.id != null }
                            .associate { it.subastaId!! to it.id!! }
                    }

                    val responseSaldo = RetrofitClient.api.obtenerSaldo(usuarioId)

                    if (responseSaldo.isSuccessful && responseSaldo.body()?.ok == true) {
                        val nuevoSaldo = responseSaldo.body()?.saldo ?: saldo
                        saldo = nuevoSaldo
                        prefs.edit().putFloat("saldo", nuevoSaldo.toFloat()).apply()
                    }
                }
            } catch (e: Exception) {
                mensaje = "No se pudo cargar la información del usuario."
            }
        }
    }

    fun cargarSubastas() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val responseSubastas = RetrofitClient.api.buscarSubastas(
                    texto = busqueda.ifBlank { null },
                    categoria = if (categoriaSeleccionada == "Todas") null else categoriaSeleccionada,
                    min = precioMinimo.toDoubleOrNull(),
                    max = precioMaximo.toDoubleOrNull(),
                    estado = estadoSeleccionado,
                    orden = ordenSeleccionado
                )

                if (responseSubastas.isSuccessful) {
                    subastas.clear()
                    subastas.addAll(responseSubastas.body() ?: emptyList())
                } else {
                    mensaje = "No se pudieron cargar las subastas."
                }

                cargarFavoritosYSaldo()
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    fun limpiarFiltros() {
        busqueda = ""
        categoriaSeleccionada = "Todas"
        estadoSeleccionado = "TODAS"
        precioMinimo = ""
        precioMaximo = ""
        ordenSeleccionado = "recientes"
        cargarSubastas()
    }

    LaunchedEffect(Unit) {
        cargarSubastas()
        while (true) {
            delay(5000)
            cargarSubastas()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text("Subastas", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { cargarSubastas() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Buscar")
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = { mostrarFiltros = !mostrarFiltros },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (mostrarFiltros) "Ocultar" else "Filtros")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                estados.forEach { estado ->
                    FilterChip(
                        selected = estadoSeleccionado == estado.first,
                        onClick = {
                            estadoSeleccionado = estado.first
                            cargarSubastas()
                        },
                        label = { Text(estado.second) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filtrar resultados", fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Categoría", fontSize = 15.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            categorias.forEach { categoria ->
                                FilterChip(
                                    selected = categoriaSeleccionada == categoria,
                                    onClick = {
                                        categoriaSeleccionada = categoria
                                        cargarSubastas()
                                    },
                                    label = { Text(categoria) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Rango de precio", fontSize = 15.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = precioMinimo,
                                onValueChange = { precioMinimo = it },
                                label = { Text("Mínimo") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedTextField(
                                value = precioMaximo,
                                onValueChange = { precioMaximo = it },
                                label = { Text("Máximo") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        var ordenExpandido by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = ordenExpandido,
                            onExpandedChange = { ordenExpandido = !ordenExpandido }
                        ) {
                            OutlinedTextField(
                                value = ordenes.firstOrNull { it.first == ordenSeleccionado }?.second ?: "Más recientes",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ordenar por") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ordenExpandido)
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
                                expanded = ordenExpandido,
                                onDismissRequest = { ordenExpandido = false }
                            ) {
                                ordenes.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion.second) },
                                        onClick = {
                                            ordenSeleccionado = opcion.first
                                            ordenExpandido = false
                                            cargarSubastas()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { cargarSubastas() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Aplicar")
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { limpiarFiltros() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Limpiar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mensaje.isNotEmpty()) {
                Text(mensaje, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (cargando) {
                Text("Cargando subastas...", color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (!cargando && subastas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No hay subastas para mostrar", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Probá con otra búsqueda o cambiá los filtros.", color = Color.Gray)
                    }
                }
            }
        }

        items(subastas) { subasta ->

            val guardada = favoritosIds.contains(subasta.id)
            val tiempoCalculado = calcularTiempoRestante(subasta.fechaFin)
            val estadoVisual = estadoVisualSubasta(subasta.estado, tiempoCalculado)
            val tiempoRestante = if (estadoVisual == "FINALIZADA") "Finalizada" else tiempoCalculado
            val puedePujar = estadoVisual == "ACTIVA"

            val estadoColor = when (estadoVisual) {
                "ACTIVA" -> Color(0xFF4CAF50)
                "PROGRAMADA" -> Color(0xFFFFC107)
                "FINALIZADA" -> Color(0xFFFF6B81)
                else -> Color.Gray
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable { onAuctionClick(subasta.toAuctionItem(estadoVisual)) },
                shape = RoundedCornerShape(22.dp)
            ) {
                Column {
                    ImagenSubastaLista(
                        imagen = subasta.imagen,
                        nombre = subasta.nombre ?: "Subasta"
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subasta.nombre ?: "Sin nombre", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(subasta.categoria ?: "Sin categoría", color = Color.Gray)
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = estadoColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = estadoVisual,
                                    color = estadoColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(if (puedePujar) "Precio actual" else "Precio final", color = Color.Gray)
                        Text("$${subasta.precioActual}", fontSize = 28.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Ofertas realizadas: ${subasta.ofertas}")
                        Text(if (puedePujar) "Ganador actual: ${subasta.ganador}" else "Ganador final: ${subasta.ganador}")
                        Text("Tiempo restante: $tiempoRestante")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Saldo disponible: $${String.format("%.2f", saldo)}",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    if (usuarioId == 0L) {
                                        mensaje = "Debe iniciar sesión para guardar."
                                        return@OutlinedButton
                                    }

                                    scope.launch {
                                        try {
                                            if (guardada) {
                                                favoritosMap[subasta.id]?.let {
                                                    RetrofitClient.api.eliminarFavorito(it)
                                                }
                                            } else {
                                                RetrofitClient.api.agregarFavorito(
                                                    FavoritoRequest(usuarioId = usuarioId, subastaId = subasta.id)
                                                )
                                            }

                                            cargarSubastas()
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo actualizar favoritos."
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(if (guardada) "❤️ Guardada" else "🤍 Guardar")
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (usuarioId == 0L) {
                                        mensaje = "Debe iniciar sesión para pujar."
                                        return@Button
                                    }

                                    if (!puedePujar) {
                                        mensaje = "La subasta ya finalizó."
                                        cargarSubastas()
                                        return@Button
                                    }

                                    val nuevaOferta = subasta.precioActual + 0.5

                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.api.crearPuja(
                                                PujaRequest(usuarioId = usuarioId, subastaId = subasta.id, monto = nuevaOferta)
                                            )

                                            val resultado = response.body()

                                            if (response.isSuccessful && resultado?.ok == true) {
                                                resultado.saldo?.let { nuevoSaldo ->
                                                    saldo = nuevoSaldo
                                                    prefs.edit().putFloat("saldo", nuevoSaldo.toFloat()).apply()
                                                }

                                                historial.add(
                                                    Bid(producto = subasta.nombre, precio = nuevaOferta, ganador = "Tú")
                                                )

                                                mensaje = resultado.mensaje ?: ""
                                                cargarSubastas()
                                            } else {
                                                mensaje = resultado?.mensaje ?: "No se pudo realizar la puja."
                                                cargarSubastas()
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
                                Text(if (puedePujar) "🔥 Pujar" else "Finalizada")
                            }
                        }

                        AnimatedVisibility(
                            visible = subasta.ganador == prefs.getString("nombre", "") && puedePujar
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Vas ganando esta subasta", color = Color(0xFF4CAF50), fontSize = 16.sp)
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

private fun SubastaResponse.toAuctionItem(
    estadoVisual: String
): AuctionItem {
    return AuctionItem(
        nombre = nombre ?: "Sin nombre",
        descripcion = descripcion ?: "Sin descripción",
        precioInicial = precioActual,
        imagen = 0,
        categoria = categoria ?: "Sin categoría",
        fechaInicio = fechaCreacion ?: "Hoy"
    ).apply {
        idApi = id
        precio = precioActual
        estado = estadoVisual
        ofertas = ofertas
        ganador = ganador ?: "Nadie"
        iniciada = estadoVisual == "ACTIVA"
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
                minutos > 0 -> "${minutos}m"
                else -> "${TimeUnit.MILLISECONDS.toSeconds(diferencia)}s"
            }
        }
    } catch (e: Exception) {
        "No disponible"
    }
}

private fun estadoVisualSubasta(
    estadoBackend: String,
    tiempoRestante: String
): String {
    return if (estadoBackend == "ACTIVA" && tiempoRestante == "Finalizada") {
        "FINALIZADA"
    } else {
        estadoBackend
    }
}