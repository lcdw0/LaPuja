package com.example.lapuja.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // 🔥 Banner principal
        Card(
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
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
                        text = "Las mejores subastas en tiempo real 🔥",
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {

                            navController.navigate("auction")
                        }
                    ) {

                        Text("Explorar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 📦 Categorías
        Text(
            text = "📦 Categorías",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            CategoryCard("🎮\nGaming")

            CategoryCard("📱\nTecnología")

            CategoryCard("⌚\nGadgets")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 Acciones rápidas
        Text(
            text = "⚡ Acciones rápidas",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                navController.navigate("auction")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("🔥 Ir a Subastas")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {

                navController.navigate("history")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("📜 Ver Historial")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {

                navController.navigate("profile")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("👤 Mi Perfil")
        }
    }
}

@Composable
fun CategoryCard(texto: String) {

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .size(100.dp)
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