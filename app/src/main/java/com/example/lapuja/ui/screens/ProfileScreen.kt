package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.ui.components.AppProfileImage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val usuarioId = prefs.getLong("usuarioId", 0L)

    val nombre by remember { mutableStateOf(prefs.getString("nombre", "Usuario LaPuja") ?: "Usuario LaPuja") }
    val correo by remember { mutableStateOf(prefs.getString("correo", "Sin correo") ?: "Sin correo") }
    val saldo by remember { mutableStateOf(prefs.getFloat("saldo", 10000f)) }
    val fotoPerfil by remember { mutableStateOf(prefs.getString("fotoPerfil", null)) }

    val telefono by remember { mutableStateOf(prefs.getString("telefono", "") ?: "") }
    val ciudad by remember { mutableStateOf(prefs.getString("ciudad", "") ?: "") }
    val biografia by remember { mutableStateOf(prefs.getString("biografia", "") ?: "") }
    val fechaRegistro by remember { mutableStateOf(prefs.getString("fechaRegistro", "") ?: "") }

    var totalSubastas by remember { mutableStateOf(0) }
    var totalPujas by remember { mutableStateOf(0) }
    var totalGanadas by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                if (usuarioId != 0L) {
                    val subastasUsuario = RetrofitClient.api.listarSubastasPorUsuario(usuarioId)
                    if (subastasUsuario.isSuccessful) {
                        totalSubastas = subastasUsuario.body()?.size ?: 0
                    }

                    val pujasUsuario = RetrofitClient.api.listarPujasPorUsuario(usuarioId)
                    if (pujasUsuario.isSuccessful) {
                        totalPujas = pujasUsuario.body()?.size ?: 0
                    }

                    val todasSubastas = RetrofitClient.api.listarSubastas()
                    if (todasSubastas.isSuccessful) {
                        totalGanadas = todasSubastas.body()
                            ?.count { it.ganadorId == usuarioId && it.estado == "FINALIZADA" }
                            ?: 0
                    }
                }
            } catch (e: Exception) {
                totalSubastas = 0
                totalPujas = 0
                totalGanadas = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF121212),
                        Color(0xFF251018),
                        Color(0xFF111111)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", fontSize = 32.sp, color = Color.White)

        Spacer(modifier = Modifier.height(20.dp))

        Box {
            AppProfileImage(
                imageUrl = fotoPerfil,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp),
                shape = CircleShape,
                color = Color(0xFF2ECC71)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = Color.White, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(nombre, fontSize = 25.sp, color = Color.White)
        Text(correo, fontSize = 16.sp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = nivelUsuario(saldo),
            color = Color(0xFFFFD166),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        ProfileInfoCard(
            telefono = telefono,
            ciudad = ciudad,
            biografia = biografia,
            fechaRegistro = fechaRegistro
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProfileStatsRow(
            subastas = totalSubastas,
            pujas = totalPujas,
            ganadas = totalGanadas
        )

        Spacer(modifier = Modifier.height(18.dp))

        WalletCard(
            saldo = saldo,
            onAddBalance = { navController.navigate("recharge_wallet") },
            onHistory = { navController.navigate("wallet_history") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        GlassButton(
            text = "✎  Editar perfil",
            onClick = { navController.navigate("edit_profile") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileOptionCard(
                title = "Mis Subastas",
                subtitle = "Gestionar publicadas",
                icon = "🔨",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("my_auctions") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            ProfileOptionCard(
                title = "Mis Ofertas",
                subtitle = "Pujas realizadas",
                icon = "🏷️",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("my_bids") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileOptionCard(
                title = "Guardadas",
                subtitle = "Favoritos",
                icon = "♡",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("saved_auctions") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            ProfileOptionCard(
                title = "Pagos",
                subtitle = "Wallet y tarjetas",
                icon = "💳",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("payment_methods") }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                prefs.edit()
                    .remove("usuarioId")
                    .remove("correo")
                    .remove("nombre")
                    .remove("fotoPerfil")
                    .remove("telefono")
                    .remove("ciudad")
                    .remove("biografia")
                    .remove("fechaRegistro")
                    .apply()

                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE84D5B)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileStatsRow(
    subastas: Int,
    pujas: Int,
    ganadas: Int
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard(subastas.toString(), "Subastas", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(10.dp))
        StatCard(pujas.toString(), "Pujas", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(10.dp))
        StatCard(ganadas.toString(), "Ganadas", Modifier.weight(1f))
    }
}

@Composable
fun ProfileInfoCard(
    telefono: String,
    ciudad: String,
    biografia: String,
    fechaRegistro: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.11f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Información del perfil",
                color = Color.White,
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoLine("📍", "Ciudad", ciudad.ifBlank { "No especificada" })
            InfoLine("📞", "Contacto", telefono.ifBlank { "No especificado" })
            InfoLine("📅", "Miembro desde", formatearFechaPerfil(fechaRegistro))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Biografía",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Text(
                text = biografia.ifBlank { "Aún no has agregado una biografía." },
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun InfoLine(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$icon $label", color = Color.LightGray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun ProfileStatsRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard("12", "Subastas", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(10.dp))
        StatCard("34", "Pujas", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(10.dp))
        StatCard("8", "Ganadas", Modifier.weight(1f))
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.11f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value, color = Color.White, fontSize = 22.sp)
                Text(text = label, color = Color.LightGray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun WalletCard(
    saldo: Float,
    onAddBalance: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.13f)
        )
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(text = "💳 Wallet LaPuja", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Saldo disponible", color = Color.LightGray, fontSize = 16.sp)

            Text(
                text = "$${String.format("%.2f", saldo)}",
                color = Color.White,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onAddBalance,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("+ Agregar")
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Historial")
                }
            }
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.13f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = text, color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun ProfileOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(124.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.11f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = icon, fontSize = 26.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = title, color = Color.White, fontSize = 17.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = subtitle, color = Color.LightGray, fontSize = 13.sp)
        }
    }
}

private fun nivelUsuario(saldo: Float): String {
    return when {
        saldo >= 50000f -> "💎 Usuario Diamante verificado"
        saldo >= 20000f -> "🥇 Usuario Oro verificado"
        saldo >= 10000f -> "🥈 Usuario Plata verificado"
        else -> "🥉 Usuario Bronce verificado"
    }
}

private fun formatearFechaPerfil(fecha: String): String {
    if (fecha.isBlank()) return "No disponible"

    return try {
        val limpia = fecha.substringBefore(".")
        val entrada = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val salida = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val date = entrada.parse(limpia)
        salida.format(date!!)
    } catch (e: Exception) {
        fecha
    }
}