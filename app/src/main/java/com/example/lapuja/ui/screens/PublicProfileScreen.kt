package com.example.lapuja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.PerfilPublicoResponse
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import com.example.lapuja.ui.components.AppProfileImage
import kotlinx.coroutines.launch

@Composable
fun PublicProfileScreen(
    usuarioId: Long,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var perfil by remember { mutableStateOf<PerfilPublicoResponse?>(null) }
    var activas by remember { mutableStateOf<List<SubastaResponse>>(emptyList()) }
    var finalizadas by remember { mutableStateOf<List<SubastaResponse>>(emptyList()) }
    var vendidas by remember { mutableStateOf<List<SubastaResponse>>(emptyList()) }

    var tabSeleccionada by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarPerfil() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val perfilResponse = RetrofitClient.api.obtenerPerfilPublico(usuarioId)

                if (perfilResponse.isSuccessful && perfilResponse.body()?.ok == true) {
                    perfil = perfilResponse.body()

                    activas = RetrofitClient.api
                        .obtenerSubastasActivasPublicas(usuarioId)
                        .body()
                        ?.subastas ?: emptyList()

                    finalizadas = RetrofitClient.api
                        .obtenerSubastasFinalizadasPublicas(usuarioId)
                        .body()
                        ?.subastas ?: emptyList()

                    vendidas = RetrofitClient.api
                        .obtenerSubastasVendidasPublicas(usuarioId)
                        .body()
                        ?.subastas ?: emptyList()
                } else {
                    mensaje = perfilResponse.body()?.mensaje ?: "No se pudo cargar el perfil."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(usuarioId) {
        cargarPerfil()
    }

    val vendedor = perfil

    if (cargando) {
        PublicProfileLoading()
        return
    }

    if (vendedor == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(mensaje.ifBlank { "Perfil no encontrado." })
        }
        return
    }

    val tabs = listOf("Activas", "Finalizadas", "Vendidas")

    val subastasMostradas = when (tabSeleccionada) {
        0 -> activas
        1 -> finalizadas
        else -> vendidas
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Button(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(18.dp))

            PublicProfileHeader(vendedor = vendedor)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PublicStatCard(
                    title = "Ventas",
                    value = "${vendedor.cantidadVentas ?: 0}",
                    modifier = Modifier.weight(1f)
                )

                PublicStatCard(
                    title = "Compras",
                    value = "${vendedor.cantidadCompras ?: 0}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PublicStatCard(
                    title = "Activas",
                    value = "${vendedor.subastasActivas ?: 0}",
                    modifier = Modifier.weight(1f)
                )

                PublicStatCard(
                    title = "Vendidas",
                    value = "${vendedor.subastasVendidas ?: 0}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedButton(
                onClick = { cargarPerfil() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Actualizar perfil")
            }

            Spacer(modifier = Modifier.height(22.dp))

            TabRow(selectedTabIndex = tabSeleccionada) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = { Text(titulo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (subastasMostradas.isEmpty()) {
            item {
                PublicEmptyState(
                    texto = when (tabSeleccionada) {
                        0 -> "Este vendedor aún no tiene subastas activas."
                        1 -> "Este vendedor aún no tiene subastas finalizadas."
                        else -> "Este vendedor aún no tiene subastas vendidas."
                    }
                )
            }
        } else {
            items(subastasMostradas) { subasta ->
                PublicAuctionCard(
                    subasta = subasta,
                    onClick = {
                        navController.navigate("auction_detail/${subasta.id}")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PublicProfileHeader(
    vendedor: PerfilPublicoResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppProfileImage(
                imageUrl = vendedor.fotoPerfil,
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = vendedor.nombre ?: "Vendedor",
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vendedor.ciudad ?: "Ciudad no disponible",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Miembro desde ${formatearFechaPublica(vendedor.fechaRegistro)}",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⭐ ${vendedor.promedioEstrellas ?: 0.0} / 5.0",
                fontSize = 18.sp
            )

            if (!vendedor.biografia.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = vendedor.biografia,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PublicStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 26.sp
            )
        }
    }
}

@Composable
fun PublicAuctionCard(
    subasta: SubastaResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(95.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                AppImage(
                    imageUrl = subasta.imagen,
                    contentDescription = subasta.nombre,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subasta.nombre,
                    fontSize = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = subasta.categoria,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "$${subasta.precioActual}",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${subasta.ofertas} ofertas • ${subasta.estado}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PublicEmptyState(
    texto: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📦",
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = texto,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PublicProfileLoading() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            repeat(4) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {}

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

fun formatearFechaPublica(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "No disponible"

    return try {
        fecha.substringBefore("T")
    } catch (e: Exception) {
        fecha
    }
}