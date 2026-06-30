package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.ui.navigation.Routes
import kotlinx.coroutines.launch
import com.example.lapuja.utils.formatearCordobasDecimal

@Composable
fun WalletScreen(
    prefs: SharedPreferences,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val usuarioId = prefs.getLong("usuarioId", 0L)

    var saldo by remember { mutableStateOf(prefs.getFloat("saldo", 0f)) }
    var saldoRetenido by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val saldoResponse = RetrofitClient.api.obtenerSaldo(usuarioId)
                if (saldoResponse.isSuccessful && saldoResponse.body()?.ok == true) {
                    val nuevoSaldo = saldoResponse.body()?.saldo ?: 0.0
                    saldo = nuevoSaldo.toFloat()
                    prefs.edit().putFloat("saldo", saldo).apply()
                }

                val retenidoResponse = RetrofitClient.api.obtenerSaldoRetenido(usuarioId)
                if (retenidoResponse.isSuccessful && retenidoResponse.body()?.ok == true) {
                    saldoRetenido = retenidoResponse.body()?.totalRetenido ?: 0.0
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
            .padding(20.dp)
    ) {
        Text("Wallet", color = Color.White, fontSize = 32.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.13f)
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Saldo disponible", color = Color.LightGray, fontSize = 15.sp)
                Text(
                    text = formatearCordobasDecimal(saldo.toDouble()),
                    color = Color.White,
                    fontSize = 38.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Saldo retenido", color = Color.LightGray, fontSize = 15.sp)
                Text(
                    text = formatearCordobasDecimal(saldoRetenido),
                    color = Color(0xFFFFD166),
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        WalletMenuCard(
            icon = "➕",
            title = "Agregar saldo",
            subtitle = "Recargar tu wallet",
            onClick = { navController.navigate(Routes.RECHARGE_WALLET) }
        )

        WalletMenuCard(
            icon = "📜",
            title = "Historial",
            subtitle = "Ver movimientos de tu wallet",
            onClick = { navController.navigate(Routes.WALLET_HISTORY) }
        )

        WalletMenuCard(
            icon = "🔒",
            title = "Saldo retenido",
            subtitle = "Fondos retenidos en subastas activas",
            onClick = { navController.navigate(Routes.HELD_FUNDS) }
        )

        WalletMenuCard(
            icon = "💳",
            title = "Métodos de pago",
            subtitle = "Administrar tarjetas registradas",
            onClick = { navController.navigate(Routes.PAYMENT_METHODS) }
        )
    }
}

@Composable
fun WalletMenuCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .height(86.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.11f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 28.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, color = Color.White, fontSize = 18.sp)
                Text(subtitle, color = Color.LightGray, fontSize = 14.sp)
            }
        }
    }
}