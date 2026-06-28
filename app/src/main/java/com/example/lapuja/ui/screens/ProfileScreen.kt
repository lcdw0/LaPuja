package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.ui.components.AppProfileImage

@Composable
fun ProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {
    val nombre by remember {
        mutableStateOf(prefs.getString("nombre", "Usuario LaPuja") ?: "Usuario LaPuja")
    }

    val correo by remember {
        mutableStateOf(prefs.getString("correo", "Sin correo") ?: "Sin correo")
    }

    val saldo by remember {
        mutableStateOf(prefs.getFloat("saldo", 10000f))
    }

    val fotoPerfil by remember {
        mutableStateOf(prefs.getString("fotoPerfil", null))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi perfil",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppProfileImage(
            imageUrl = fotoPerfil,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = nombre,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = correo,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        BalanceCard(
            saldo = saldo,
            onAddBalance = {
                navController.navigate("payment_methods")
            },
            onHistory = {
                navController.navigate("history")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate("edit_profile")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Editar perfil")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("my_auctions")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Mis subastas")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("my_bids")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Mis ofertas")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("saved_auctions")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Subastas guardadas")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("payment_methods")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Métodos de pago")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                prefs.edit()
                    .remove("usuarioId")
                    .remove("correo")
                    .remove("nombre")
                    .remove("fotoPerfil")
                    .apply()

                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun BalanceCard(
    saldo: Float,
    onAddBalance: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B81),
                            Color(0xFF1E1E1E)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "💳 Wallet LaPuja",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Saldo disponible",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${String.format("%.2f", saldo)}",
                    color = Color.White,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onAddBalance,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Agregar")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedButton(
                        onClick = onHistory,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Historial")
                    }
                }
            }
        }
    }
}