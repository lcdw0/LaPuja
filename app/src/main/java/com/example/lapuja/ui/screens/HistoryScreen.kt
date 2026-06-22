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
import com.example.lapuja.data.Bid

@Composable
fun HistoryScreen(
    historial: MutableList<Bid>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Historial de pujas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aquí aparecen las ofertas que has realizado.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (historial.isEmpty()) {
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
                            text = "Todavía no has realizado pujas",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cuando participes en una subasta, tus ofertas aparecerán aquí.",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(historial.reversed()) { bid ->
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
                            text = bid.producto,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Monto ofertado: $${bid.precio}",
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Estado: ${if (bid.ganador == "Tú") "Ganando actualmente" else "Participaste"}",
                            fontSize = 15.sp,
                            color = Color.Gray
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