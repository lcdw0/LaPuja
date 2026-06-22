package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.R
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.Bid
import kotlinx.coroutines.delay

@Composable
fun AuctionScreen(
    prefs: SharedPreferences,
    historial: MutableList<Bid>,
    onAuctionClick: (AuctionItem) -> Unit
) {
    var saldo by remember {
        mutableStateOf(prefs.getFloat("saldo", 10000.0f).toDouble())
    }

    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    val filtros = listOf(
        "Todas",
        "Tecnología",
        "Computadoras",
        "Accesorios",
        "Activas",
        "Programadas",
        "Guardadas"
    )

    val productos = remember {
        mutableStateListOf(
            AuctionItem(
                nombre = "iPhone 13 Pro",
                descripcion = "iPhone 13 Pro en excelente estado, con batería en buen rendimiento y cargador incluido.",
                precioInicial = 5.0,
                imagen = R.drawable.iphone,
                categoria = "Tecnología",
                fechaInicio = "Hoy"
            ).apply {
                estado = "ACTIVA"
                iniciada = true
                tiempo = 30
            },
            AuctionItem(
                nombre = "Laptop Gamer",
                descripcion = "Laptop gamer ideal para juegos y trabajos pesados. Incluye cargador original.",
                precioInicial = 10.0,
                imagen = R.drawable.laptop,
                categoria = "Computadoras",
                fechaInicio = "En 2 horas"
            ).apply {
                estado = "PROGRAMADA"
                iniciada = false
                tiempo = 60
            },
            AuctionItem(
                nombre = "Audífonos Pro",
                descripcion = "Audífonos inalámbricos con cancelación de ruido y estuche de carga.",
                precioInicial = 3.0,
                imagen = R.drawable.audifonos,
                categoria = "Accesorios",
                fechaInicio = "Hoy"
            ).apply {
                estado = "ACTIVA"
                iniciada = true
                tiempo = 45
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)

            productos.forEach { producto ->
                if (producto.iniciada && producto.tiempo > 0) {
                    producto.tiempo--
                }

                if (producto.iniciada && producto.tiempo <= 0) {
                    producto.estado = "FINALIZADA"
                }
            }
        }
    }

    val productosFiltrados = productos.filter { producto ->
        val coincideBusqueda =
            producto.nombre.contains(busqueda, ignoreCase = true) ||
                    producto.categoria.contains(busqueda, ignoreCase = true)

        val coincideFiltro =
            when (filtroSeleccionado) {
                "Todas" -> true
                "Guardadas" -> producto.guardado
                "Activas" -> producto.estado == "ACTIVA"
                "Programadas" -> producto.estado == "PROGRAMADA"
                else -> producto.categoria == filtroSeleccionado
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Saldo disponible",
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$$saldo",
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

        if (productosFiltrados.isEmpty()) {
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

        items(productosFiltrados) { producto ->

            val puedePujar =
                producto.iniciada &&
                        producto.tiempo > 0 &&
                        producto.estado == "ACTIVA"

            val estadoColor =
                when (producto.estado) {
                    "ACTIVA" -> Color(0xFF4CAF50)
                    "PROGRAMADA" -> Color(0xFFFFC107)
                    else -> Color(0xFFFF6B81)
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable {
                        onAuctionClick(producto)
                    },
                shape = RoundedCornerShape(22.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = producto.imagen),
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
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
                                    text = producto.nombre,
                                    fontSize = 24.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = producto.categoria,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = estadoColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = producto.estado,
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
                            text = "$${producto.precio}",
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Ofertas realizadas: ${producto.ofertas}")
                        Text("Ganador actual: ${producto.ganador}")

                        if (producto.iniciada) {
                            Text("Tiempo restante: ${producto.tiempo}s")
                        } else {
                            Text("Inicia: ${producto.fechaInicio}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    producto.guardado = !producto.guardado
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    if (producto.guardado) "❤️ Guardada" else "🤍 Guardar"
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    val incremento = 0.5

                                    if (saldo >= incremento && puedePujar) {
                                        saldo -= incremento
                                        producto.precio += incremento
                                        producto.ganador = "Tú"
                                        producto.ofertas++

                                        historial.add(
                                            Bid(
                                                producto = producto.nombre,
                                                precio = producto.precio,
                                                ganador = "Tú"
                                            )
                                        )

                                        prefs.edit()
                                            .putFloat("saldo", saldo.toFloat())
                                            .apply()
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
                            visible = producto.ganador == "Tú" && producto.estado == "ACTIVA"
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Nueva oferta realizada",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        if (producto.estado == "FINALIZADA") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Subasta terminada",
                                fontSize = 18.sp
                            )

                            Text(
                                text = "Ganador final: ${producto.ganador}",
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