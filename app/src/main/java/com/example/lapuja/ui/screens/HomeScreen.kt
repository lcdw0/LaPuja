package com.example.lapuja.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val subastas = remember { mutableStateListOf<SubastaResponse>() }

    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarSubastas() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val response = RetrofitClient.api.listarSubastas()

                if (response.isSuccessful) {
                    subastas.clear()
                    subastas.addAll(response.body() ?: emptyList())
                } else {
                    mensaje = "No se pudieron cargar las subastas."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarSubastas()
    }

    val subastasActivas = subastas
        .filter { it.estado == "ACTIVA" }
        .sortedByDescending { it.ofertas }
        .take(5)

    val categorias = subastas
        .map { it.categoria }
        .distinct()
        .take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Card(
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B81),
                                Color(0xFF121212)
                            )
                        )
                    )
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💰 La Puja",
                        fontSize = 38.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Encuentra, oferta y gana en tiempo real",
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            navController.navigate("auction")
                        }
                    ) {
                        Text("Explorar subastas")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "📦 Categorías",
            fontSize = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categorias.isEmpty()) {
            Text(
                text = "Todavía no hay categorías disponibles.",
                color = Color.Gray
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                categorias.forEach { categoria ->
                    CategoryCard(
                        texto = "${emojiCategoria(categoria)}\n$categoria",
                        onClick = {
                            navController.navigate("auction")
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥 Subastas destacadas",
                fontSize = 25.sp
            )

            TextButton(
                onClick = {
                    navController.navigate("auction")
                }
            ) {
                Text("Ver todas")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            cargando -> {
                Text(
                    text = "Cargando subastas...",
                    color = Color.Gray
                )
            }

            mensaje.isNotEmpty() -> {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            subastasActivas.isEmpty() -> {
                Text(
                    text = "Todavía no hay subastas activas.",
                    color = Color.Gray
                )
            }

            else -> {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    subastasActivas.forEach { subasta ->
                        FeaturedAuctionCard(
                            title = subasta.nombre,
                            price = "$${subasta.precioActual}",
                            time = calcularTiempoRestanteHome(subasta.fechaFin),
                            imageUrl = subasta.imagen,
                            onClick = {
                                navController.navigate("auction_detail/${subasta.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    texto: String,
    onClick: () -> Unit
) {
    val partes = texto.split("\n")
    val emoji = partes.getOrNull(0) ?: "📦"
    val categoria = partes.getOrNull(1) ?: texto

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(125.dp)
            .height(115.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = categoria,
                fontSize = 14.sp,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedAuctionCard(
    title: String,
    price: String,
    time: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(210.dp)
            .padding(end = 14.dp)
            .clickable {
                onClick()
            }
    ) {
        Column {
            AppImage(
                imageUrl = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Oferta actual: $price",
                    fontSize = 14.sp
                )

                Text(
                    text = "Tiempo: $time",
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun emojiCategoria(categoria: String): String {
    return when (categoria.lowercase()) {
        "tecnología" -> "📱"
        "computadoras" -> "💻"
        "celulares" -> "📱"
        "videojuegos" -> "🎮"
        "electrodomésticos" -> "🔌"
        "vehículos" -> "🚗"
        "ropa" -> "👕"
        "hogar" -> "🏠"
        "coleccionables" -> "🧸"
        else -> "📦"
    }
}

private fun calcularTiempoRestanteHome(fechaFin: String?): String {
    if (fechaFin.isNullOrBlank()) return "No disponible"

    return try {
        val fechaLimpia = fechaFin.substringBefore(".")
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val fin = formato.parse(fechaLimpia)?.time ?: return "No disponible"

        val diferencia = fin - System.currentTimeMillis()

        if (diferencia <= 0) {
            "Finalizada"
        } else {
            val dias = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diferencia)
            val horas = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diferencia) % 24
            val minutos = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diferencia) % 60

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