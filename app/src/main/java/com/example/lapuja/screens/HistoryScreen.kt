package com.example.lapuja.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.Bid

@Composable
fun HistoryScreen(historial: List<Bid>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "📜 Historial",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(historial) { bid ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            ) {

                Column(
                    modifier = Modifier.padding(15.dp)
                ) {

                    Text(
                        text = "📦 ${bid.producto}",
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💵 Precio: $${bid.precio}",
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🏆 Ganador: ${bid.ganador}",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}