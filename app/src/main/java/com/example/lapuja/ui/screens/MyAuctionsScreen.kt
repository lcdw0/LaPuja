package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.AuctionItem

@Composable
fun MyAuctionsScreen(
    productos: MutableList<AuctionItem>
) {

    val misSubastas = productos

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {
            Text(
                text = "Mis subastas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gestiona las subastas que has publicado.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (misSubastas.isEmpty()) {

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

        } else {

            items(misSubastas.reversed()) { auction ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = auction.nombre,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Categoría: ${auction.categoria}"
                        )

                        Text(
                            text = "Precio actual: $${auction.precio}"
                        )

                        Text(
                            text = "Ofertas: ${auction.ofertas}"
                        )

                        Text(
                            text = "Ganador: ${auction.ganador}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Estado: ${auction.estado}",
                            color = when (auction.estado) {
                                "ACTIVA" -> Color(0xFF4CAF50)
                                "PROGRAMADA" -> Color(0xFFFFC107)
                                else -> Color(0xFFFF6B81)
                            }
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