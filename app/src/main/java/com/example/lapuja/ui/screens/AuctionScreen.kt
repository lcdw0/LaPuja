package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    val productosFiltrados = productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
                it.categoria.contains(busqueda, ignoreCase = true)
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

            Text(
                text = "Saldo disponible: $$saldo",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto 🔍") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(productosFiltrados) { producto ->

            val puedePujar = producto.iniciada && producto.tiempo > 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable {
                        onAuctionClick(producto)
                    },
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Image(
                        painter = painterResource(id = producto.imagen),
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = producto.nombre,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Categoría: ${producto.categoria}")
                    Text("Estado: ${producto.estado}")
                    Text("Fecha de inicio: ${producto.fechaInicio}")

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Precio actual: $${producto.precio}",
                        fontSize = 20.sp
                    )

                    Text("Ofertas realizadas: ${producto.ofertas}")

                    if (producto.iniciada) {
                        Text("Tiempo restante: ${producto.tiempo}s")
                    } else {
                        Text("Inicia pronto")
                    }

                    Text("Ganador actual: ${producto.ganador}")

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
                        Text(
                            text = "🔥 Nueva oferta realizada",
                            fontSize = 16.sp
                        )
                    }

                    if (producto.estado == "FINALIZADA") {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "🎉 Subasta terminada",
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
}