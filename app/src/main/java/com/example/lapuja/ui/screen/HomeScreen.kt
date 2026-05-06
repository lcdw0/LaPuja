package com.example.lapuja.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            description = "Excelente estado",
            category = "Tecnología",
            initialPrice = 300.0,
            currentBid = 420.0,
            timeLeft = "2h 15m",
            seller = "Mario"
        ),
        Auction(
            id = 2,
            title = "PlayStation 5",
            description = "Incluye control",
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
            text = "Hola, Mario 👋",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Encuentra las mejores subastas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Buscar subasta") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {

            FilterChip("Todas")
            FilterChip("Recientes")
            FilterChip("Finalizan pronto")
            FilterChip("Guardadas")
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(auctions) { auction ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            onAuctionClick()
                        },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {

                    Column {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color.LightGray)
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = auction.title,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = auction.category,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Oferta actual",
                                color = Color.Gray
                            )

                            Text(
                                text = "$${auction.currentBid}",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "⏰ ${auction.timeLeft}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String) {

    Surface(
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )
        )
    }
}