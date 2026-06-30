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
import com.example.lapuja.ui.navigation.Routes
import com.example.lapuja.utils.formatearCordobasDecimal
import kotlinx.coroutines.launch
import com.example.lapuja.utils.nombreCompleto

@Composable
fun ProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val usuarioId = prefs.getLong("usuarioId", 0L)

    var nombre by remember { mutableStateOf(prefs.getString("nombre", "Usuario LaPuja") ?: "Usuario LaPuja") }
    var apellidos by remember { mutableStateOf(prefs.getString("apellidos", "") ?: "") }
    var correo by remember { mutableStateOf(prefs.getString("correo", "Sin correo") ?: "Sin correo") }
    var saldo by remember { mutableStateOf(prefs.getFloat("saldo", 10000f)) }
    var saldoRetenido by remember { mutableStateOf(0.0) }

    var fotoPerfil by remember { mutableStateOf(prefs.getString("fotoPerfil", null)) }
    var telefono by remember { mutableStateOf(prefs.getString("telefono", "") ?: "") }
    var ciudad by remember { mutableStateOf(prefs.getString("ciudad", "") ?: "") }
    var biografia by remember { mutableStateOf(prefs.getString("biografia", "") ?: "") }
    var fechaRegistro by remember { mutableStateOf(prefs.getString("fechaRegistro", "") ?: "") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                if (usuarioId != 0L) {
                    val responseUsuario = RetrofitClient.api.obtenerUsuario(usuarioId)

                    if (responseUsuario.isSuccessful && responseUsuario.body()?.ok == true) {
                        val usuario = responseUsuario.body()!!

                        nombre = usuario.nombre ?: nombre
                        apellidos = usuario.apellidos ?: ""
                        correo = usuario.correo ?: correo
                        fotoPerfil = usuario.fotoPerfil
                        telefono = usuario.telefono ?: ""
                        ciudad = usuario.ciudad ?: ""
                        biografia = usuario.biografia ?: ""
                        fechaRegistro = usuario.fechaRegistro ?: ""

                        prefs.edit()
                            .putString("nombre", nombre)
                            .putString("apellidos", apellidos)
                            .putString("correo", correo)
                            .putString("fotoPerfil", fotoPerfil ?: "")
                            .putString("telefono", telefono)
                            .putString("ciudad", ciudad)
                            .putString("biografia", biografia)
                            .putString("fechaRegistro", fechaRegistro)
                            .apply()
                    }

                    val responseSaldo = RetrofitClient.api.obtenerSaldo(usuarioId)
                    if (responseSaldo.isSuccessful && responseSaldo.body()?.ok == true) {
                        val nuevoSaldo = responseSaldo.body()?.saldo ?: saldo.toDouble()
                        saldo = nuevoSaldo.toFloat()
                        prefs.edit().putFloat("saldo", nuevoSaldo.toFloat()).apply()
                    }

                    val responseRetenido = RetrofitClient.api.obtenerSaldoRetenido(usuarioId)
                    if (responseRetenido.isSuccessful && responseRetenido.body()?.ok == true) {
                        saldoRetenido = responseRetenido.body()?.totalRetenido ?: 0.0
                    }
                }
            } catch (e: Exception) {
                saldoRetenido = 0.0
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

        Text(
            text = nombreCompleto(nombre, apellidos),
            fontSize = 25.sp,
            color = Color.White
        )
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

        WalletSummaryCard(
            saldo = saldo,
            saldoRetenido = saldoRetenido,
            onOpenWallet = { navController.navigate(Routes.WALLET) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        GlassButton(
            text = "✎  Editar perfil",
            onClick = { navController.navigate("edit_profile") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        ProfileOptionCard(
            title = "Mi actividad",
            subtitle = "Ver estadísticas, gráficas y resumen general",
            icon = "📊",
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Routes.DASHBOARD) }
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                subtitle = "Métodos de pago",
                icon = "💳",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("payment_methods") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileOptionCard(
                title = "Chats",
                subtitle = "Conversaciones",
                icon = "💬",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.CHAT_LIST) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            ProfileOptionCard(
                title = "Avisos",
                subtitle = "Notificaciones",
                icon = "🔔",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                prefs.edit().clear().apply()
                navController.navigate(Routes.LOGIN) {
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
fun WalletSummaryCard(
    saldo: Float,
    saldoRetenido: Double,
    onOpenWallet: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenWallet() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.13f)
        )
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text("💳 Resumen Wallet", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Disponible", color = Color.LightGray, fontSize = 14.sp)
                    Text(
                        text = formatearCordobasDecimal(saldo.toDouble()),
                        color = Color.White,
                        fontSize = 27.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Retenido", color = Color.LightGray, fontSize = 14.sp)
                    Text(
                        text = formatearCordobasDecimal(saldoRetenido),
                        color = Color(0xFFFFD166),
                        fontSize = 27.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tocar para administrar wallet",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
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