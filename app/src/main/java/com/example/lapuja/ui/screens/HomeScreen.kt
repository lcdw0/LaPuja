package com.example.lapuja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.R

@Composable
fun HomeScreen(navController: NavController) {

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryCard("🎮\nGaming")
            CategoryCard("📱\nTecnología")
            CategoryCard("⌚\nGadgets")
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

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FeaturedAuctionCard(
                title = "iPhone 13 Pro",
                price = "$5.0",
                time = "30s",
                image = R.drawable.iphone,
                onClick = {
                    navController.navigate("auction")
                }
            )

            FeaturedAuctionCard(
                title = "Laptop Gamer",
                price = "$10.0",
                time = "Programada",
                image = R.drawable.laptop,
                onClick = {
                    navController.navigate("auction")
                }
            )

            FeaturedAuctionCard(
                title = "Audífonos Pro",
                price = "$3.0",
                time = "45s",
                image = R.drawable.audifonos,
                onClick = {
                    navController.navigate("auction")
                }
            )
        }
    }
}

@Composable
fun CategoryCard(texto: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.size(100.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = texto,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun FeaturedAuctionCard(
    title: String,
    price: String,
    time: String,
    image: Int,
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
            Image(
                painter = painterResource(id = image),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp),
                contentScale = ContentScale.Crop
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