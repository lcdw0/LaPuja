package com.example.lapuja.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lapuja.data.Auction

@Composable
fun HomeScreen(
    onAuctionClick: () -> Unit
) {

    val auctions = listOf(
        Auction(
            id = 1,
            title = "iPhone 13",
            description = "iPhone en excelente estado",
            category = "Tecnología",
            initialPrice = 300.0,
            currentBid = 420.0,
            timeLeft = "2h 15m",
            seller = "Mario"
        ),
        Auction(
            id = 2,
            title = "PlayStation 5",
            description = "PS5 con control incluido",
            category = "Videojuegos",
            initialPrice = 450.0,
            currentBid = 520.0,
            timeLeft = "45m",
            seller = "Carlos"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Subastas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Buscar subasta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(auctions) { auction ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            onAuctionClick()
                        }
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = auction.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Categoría: ${auction.category}")
                        Text("Oferta actual: $${auction.currentBid}")
                        Text("Tiempo restante: ${auction.timeLeft}")
                    }
                }
            }
        }
    }
}