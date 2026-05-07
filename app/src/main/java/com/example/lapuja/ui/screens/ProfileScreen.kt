package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
fun ProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {

    val nombre =
        prefs.getString("nombre", "Usuario")

    val correo =
        prefs.getString("correo", "correo@email.com")

    val saldo =
        prefs.getFloat("saldo", 0.0f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
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
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(100.dp)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "👤",
                                fontSize = 42.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = nombre ?: "Usuario",
                        fontSize = 28.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = correo ?: "",
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "💰 Saldo disponible",
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$$saldo",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            StatsCard("12", "Subastas")

            StatsCard("28", "Ofertas")

            StatsCard("5", "Ganadas")
        }

        Spacer(modifier = Modifier.height(30.dp))

        ProfileButton(
            text = "✏️ Editar Perfil",
            onClick = {
                navController.navigate("edit_profile")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ProfileButton(
            text = "🔥 Mis Subastas",
            onClick = {
                navController.navigate("my_auctions")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ProfileButton(
            text = "💰 Mis Ofertas",
            onClick = {
                navController.navigate("my_bids")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ProfileButton(
            text = "💳 Métodos de Pago",
            onClick = {
                navController.navigate("payment_methods")
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {

                prefs.edit()
                    .remove("correo")
                    .remove("password")
                    .apply()

                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun StatsCard(
    number: String,
    label: String
) {

    Card(
        modifier = Modifier.width(105.dp),
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = number,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProfileButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {

        Text(text)
    }
}