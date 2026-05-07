package com.example.lapuja.screens

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    historial: MutableList<Bid>
) {

    var saldo by remember {
        mutableStateOf(
            prefs.getFloat("saldo", 10.0f).toDouble()
        )
    }
    var busqueda by remember {

        mutableStateOf("")
    }


    val productos = remember {

        mutableStateListOf(

            AuctionItem(
                "iPhone 13 Pro",
                5.0,
                R.drawable.iphone
            ),

            AuctionItem(
                "Laptop Gamer",
                10.0,
                R.drawable.laptop
            ),

            AuctionItem(
                "Audífonos Pro",
                3.0,
                R.drawable.audifonos
            )
        )
    }

    // ⏱️ temporizador automático
    LaunchedEffect(Unit) {

        while (true) {

            delay(1000)

            productos.forEach { producto ->

                if (producto.tiempo > 0) {

                    producto.tiempo--
                }
            }
        }
    }

    OutlinedTextField(

        value = busqueda,

        onValueChange = {

            busqueda = it
        },

        label = {

            Text("Buscar producto 🔍")
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "💰 La Puja",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "👛 Tu saldo: $$saldo",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(productos.indices.toList()) { index ->

            val producto = productos[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(15.dp)
                ) {

                    // 🖼️ Imagen
                    Image(
                        painter = painterResource(id = producto.imagen),
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // 📦 Nombre
                    Text(
                        text = producto.nombre,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 💵 Precio
                    Text(
                        text = "💵 Precio actual: $${producto.precio}",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⏱️ Tiempo
                    Text(
                        text = "⏱️ Tiempo restante: ${producto.tiempo}s",
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🏆 Ganador
                    Text(
                        text = "🏆 Ganador: ${producto.ganador}",
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔥 BOTÓN PUJAR
                    Button(
                        onClick = {

                            // ❌ si terminó
                            if (producto.tiempo <= 0) return@Button

                            val incremento = 0.5

                            if (saldo >= incremento) {

                                saldo -= incremento

                                producto.precio += incremento

                                producto.ganador = "Tú"

                                // 📜 guardar historial
                                historial.add(

                                    Bid(
                                        producto = producto.nombre,
                                        precio = producto.precio,
                                        ganador = "Tú"
                                    )
                                )

                                prefs.edit()
                                    .putFloat(
                                        "saldo",
                                        saldo.toFloat()
                                    )
                                    .apply()
                            }

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text("🔥 Pujar")
                    }
                    AnimatedVisibility(
                        visible = producto.ganador == "Tú"
                    ) {

                        Text(
                            text = "🔥 Nueva oferta realizada",
                            fontSize = 16.sp
                        )
                    }

                    // 🎉 FINAL
                    if (producto.tiempo <= 0) {

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "🎉 Subasta terminada",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🏆 Ganador final: ${producto.ganador}",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}