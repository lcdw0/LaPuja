package com.example.lapuja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.AuctionItem

@Composable
fun AuctionDetailScreen(
    auction: AuctionItem,
    onBackClick: () -> Unit
) {
    val puedePujar =
        auction.iniciada && auction.tiempo > 0 && auction.estado == "ACTIVA"

    val estadoColor =
        when (auction.estado) {
            "ACTIVA" -> Color(0xFF4CAF50)
            "PROGRAMADA" -> Color(0xFFFFC107)
            else -> Color(0xFFFF6B81)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Image(
                    painter = painterResource(id = auction.imagen),
                    contentDescription = auction.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = auction.nombre,
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = auction.categoria,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = estadoColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = auction.estado,
                        color = estadoColor,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Oferta actual",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$${auction.precio}",
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ofertas realizadas: ${auction.ofertas}"
                    )

                    Text(
                        text = "Ganador actual: ${auction.ganador}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailInfoCard(
                    title = "Tiempo",
                    value = if (auction.iniciada) "${auction.tiempo}s" else "Programada"
                )

                DetailInfoCard(
                    title = "Inicio",
                    value = auction.fechaInicio
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Vendedor",
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Vendedor verificado",
                            fontSize = 18.sp
                        )

                        Text(
                            text = "⭐ 4.8 • 25 subastas realizadas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Descripción",
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = auction.descripcion,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            OutlinedButton(
                onClick = {
                    auction.guardado = !auction.guardado
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (auction.guardado) "❤️ Subasta guardada" else "🤍 Guardar subasta"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                enabled = puedePujar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (puedePujar) "🔥 Pujar ahora" else "Puja no disponible"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DetailInfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.width(165.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp
            )
        }
    }
}