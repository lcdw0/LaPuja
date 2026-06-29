package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.lapuja.data.Bid
import com.example.lapuja.data.remote.*
import com.example.lapuja.ui.components.AppImage
import com.example.lapuja.ui.components.AppProfileImage
import com.example.lapuja.ui.screens.components.BidSection
import com.example.lapuja.ui.screens.components.OwnerActionsSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.window.Dialog

@Composable
fun AuctionDetailScreen(
    subastaId: Long,
    prefs: SharedPreferences,
    historial: MutableList<Bid>,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var subasta by remember { mutableStateOf<SubastaResponse?>(null) }
    var vendedor by remember { mutableStateOf<UsuarioResponse?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }
    var montoPersonalizado by remember { mutableStateOf("") }
    var guardada by remember { mutableStateOf(false) }
    var favoritoId by remember { mutableStateOf<Long?>(null) }

    var saldoActual by remember {
        mutableStateOf(prefs.getFloat("saldo", 10000f).toDouble())
    }

    val usuarioId = prefs.getLong("usuarioId", 0L)

    fun cargarSubasta(mostrarCarga: Boolean = true) {
        scope.launch {
            try {
                if (mostrarCarga) cargando = true

                val response = RetrofitClient.api.obtenerSubasta(subastaId)

                if (response.isSuccessful) {
                    val subastaObtenida = response.body()
                    subasta = subastaObtenida

                    val vendedorId = subastaObtenida?.usuarioId

                    if (vendedorId != null) {
                        val responseVendedor = RetrofitClient.api.obtenerUsuario(vendedorId)

                        if (responseVendedor.isSuccessful) {
                            vendedor = responseVendedor.body()
                        }
                    }

                    if (usuarioId != 0L) {
                        val responseFavoritos = RetrofitClient.api.listarFavoritos(usuarioId)

                        if (responseFavoritos.isSuccessful) {
                            val favorito = responseFavoritos.body()
                                ?.firstOrNull { it.subastaId == subastaId }

                            guardada = favorito != null
                            favoritoId = favorito?.id
                        }

                        val responseSaldo = RetrofitClient.api.obtenerSaldo(usuarioId)

                        if (responseSaldo.isSuccessful && responseSaldo.body()?.ok == true) {
                            val nuevoSaldo = responseSaldo.body()?.saldo ?: saldoActual

                            saldoActual = nuevoSaldo

                            prefs.edit()
                                .putFloat("saldo", nuevoSaldo.toFloat())
                                .apply()
                        }
                    }
                } else {
                    mensaje = "No se pudo cargar la subasta."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                if (mostrarCarga) cargando = false
            }
        }
    }

    LaunchedEffect(subastaId) {
        cargarSubasta(true)

        while (true) {
            delay(5000)
            cargarSubasta(false)
        }
    }

    fun realizarPuja(monto: Double) {
        val item = subasta ?: return

        val finalizadaPorFecha = subastaFinalizadaPorFecha(item.fechaFin)

        if (usuarioId == 0L) {
            mensaje = "Debes iniciar sesión para pujar."
            return
        }

        if (item.usuarioId == usuarioId) {
            mensaje = "No puedes pujar en tu propia subasta."
            return
        }

        if (item.estado != "ACTIVA" || finalizadaPorFecha) {
            mensaje = "La subasta ya finalizó."
            cargarSubasta(false)
            return
        }

        if (monto <= item.precioActual) {
            mensaje = "La oferta debe ser mayor al precio actual."
            return
        }

        scope.launch {
            try {
                val response = RetrofitClient.api.crearPuja(
                    PujaRequest(
                        usuarioId = usuarioId,
                        subastaId = item.id,
                        monto = monto
                    )
                )

                val resultado = response.body()

                if (response.isSuccessful && resultado?.ok == true) {
                    resultado.saldo?.let { nuevoSaldo ->
                        saldoActual = nuevoSaldo

                        prefs.edit()
                            .putFloat("saldo", nuevoSaldo.toFloat())
                            .apply()
                    }

                    mensaje = resultado.mensaje ?: "Oferta realizada correctamente."

                    historial.add(
                        Bid(
                            producto = item.nombre,
                            precio = monto,
                            ganador = "Tú"
                        )
                    )

                    montoPersonalizado = ""
                    cargarSubasta(false)
                } else {
                    mensaje = resultado?.mensaje ?: "No se pudo realizar la puja."
                    cargarSubasta(false)
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            }
        }
    }

    if (cargando) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando subasta...")
        }
        return
    }

    val auction = subasta

    if (auction == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(mensaje.ifBlank { "Subasta no encontrada." })
        }
        return
    }

    val finalizadaPorFecha = subastaFinalizadaPorFecha(auction.fechaFin)
    val estadoVisual =
        if (auction.estado == "ACTIVA" && finalizadaPorFecha) "FINALIZADA"
        else auction.estado

    val esPropietario = auction.usuarioId == usuarioId

    val puedePujar =
        estadoVisual == "ACTIVA" && !esPropietario

    val estadoColor =
        when (estadoVisual) {
            "ACTIVA" -> Color(0xFF4CAF50)
            "PROGRAMADA" -> Color(0xFFFFC107)
            "CANCELADA" -> Color.Gray
            else -> Color(0xFFFF6B81)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
                shape = RoundedCornerShape(24.dp)
            ) {

                if (!auction.imagenes.isNullOrEmpty()) {

                    CarouselImagenes(
                        imagenes = auction.imagenes
                    )

                } else {

                    ImagenSubasta(
                        imagen = auction.imagen,
                        nombre = auction.nombre
                    )

                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = auction.nombre, fontSize = 30.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = auction.categoria,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = estadoColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = estadoVisual,
                        color = estadoColor,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (puedePujar) "Oferta actual" else "Oferta final",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "$${auction.precioActual}", fontSize = 34.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Ofertas realizadas: ${auction.ofertas}")
                    Text(
                        text = if (puedePujar)
                            "Ganador actual: ${auction.ganador}"
                        else
                            "Ganador final: ${auction.ganador}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OwnerActionsSection(
                auction = auction,
                usuarioId = usuarioId,
                navController = navController,
                onSubastaActualizada = {
                    cargarSubasta(false)
                },
                onMensaje = {
                    mensaje = it
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailInfoCard(
                    title = "Finaliza",
                    value = formatearFecha(auction.fechaFin)
                )

                DetailInfoCard(
                    title = "Inicio",
                    value = formatearFecha(auction.fechaCreacion)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(text = "Vendedor", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppProfileImage(
                        imageUrl = vendedor?.fotoPerfil,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = vendedor?.nombre ?: "Vendedor no disponible",
                            fontSize = 18.sp
                        )

                        Text(
                            text = vendedor?.correo ?: "Correo no disponible",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(text = "Descripción", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = auction.descripcion, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(26.dp))

            if (!esPropietario) {
                OutlinedButton(
                    onClick = {
                        if (usuarioId == 0L) {
                            mensaje = "Debes iniciar sesión para guardar."
                            return@OutlinedButton
                        }

                        scope.launch {
                            try {
                                if (guardada && favoritoId != null) {
                                    RetrofitClient.api.eliminarFavorito(favoritoId!!)
                                    mensaje = "Subasta eliminada de favoritos."
                                } else {
                                    RetrofitClient.api.agregarFavorito(
                                        FavoritoRequest(
                                            usuarioId = usuarioId,
                                            subastaId = auction.id
                                        )
                                    )
                                    mensaje = "Subasta guardada."
                                }

                                cargarSubasta(false)
                            } catch (e: Exception) {
                                mensaje = "No se pudo actualizar favoritos."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (guardada) "❤️ Subasta guardada" else "🤍 Guardar subasta")
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            BidSection(
                auction = auction,
                usuarioId = usuarioId,
                saldoActual = saldoActual,
                montoPersonalizado = montoPersonalizado,
                onMontoChange = {
                    montoPersonalizado = it
                    mensaje = ""
                },
                onPujar = { monto ->
                    realizarPuja(monto)
                },
                onMensaje = {
                    mensaje = it
                },
                puedePujar = puedePujar
            )

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensaje,
                    color = if (
                        mensaje.contains("correctamente") ||
                        mensaje.contains("guardada") ||
                        mensaje.contains("eliminada") ||
                        mensaje.contains("finalizada") ||
                        mensaje.contains("cancelada")
                    ) {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            if (!puedePujar && !esPropietario) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Subasta terminada", fontSize = 18.sp)
                Text(text = "Ganador final: ${auction.ganador}", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ImagenSubasta(
    imagen: String?,
    nombre: String
) {
    AppImage(
        imageUrl = imagen,
        contentDescription = nombre,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselImagenes(
    imagenes: List<SubastaImagenResponse>
) {
    var mostrarPantallaCompleta by remember { mutableStateOf(false) }
    var paginaInicial by remember { mutableStateOf(0) }

    val pagerState = rememberPagerState(
        pageCount = { imagenes.size }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AppImage(
                imageUrl = imagenes.getOrNull(page)?.url,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        paginaInicial = page
                        mostrarPantallaCompleta = true
                    }
            )
        }

        if (imagenes.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(imagenes.size) { index ->
                    Text(
                        text = if (pagerState.currentPage == index) "●" else "○",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }

    if (mostrarPantallaCompleta) {
        VisorImagenesPantallaCompleta(
            imagenes = imagenes,
            paginaInicial = paginaInicial,
            onCerrar = {
                mostrarPantallaCompleta = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisorImagenesPantallaCompleta(
    imagenes: List<SubastaImagenResponse>,
    paginaInicial: Int,
    onCerrar: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = paginaInicial,
        pageCount = { imagenes.size }
    )

    Dialog(
        onDismissRequest = onCerrar
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AppImage(
                    imageUrl = imagenes.getOrNull(page)?.url,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = onCerrar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Cerrar")
            }

            if (imagenes.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imagenes.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                )
            }
        }
    }
}

fun formatearFecha(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "No disponible"

    return try {
        val fechaLimpia = fecha.substringBefore(".")
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val formatoSalida = SimpleDateFormat("dd MMM yyyy\nhh:mm a", Locale.getDefault())

        val date = formatoEntrada.parse(fechaLimpia)
        formatoSalida.format(date!!)
    } catch (e: Exception) {
        fecha
    }
}

private fun subastaFinalizadaPorFecha(fechaFin: String?): Boolean {
    if (fechaFin.isNullOrBlank()) return false

    return try {
        val fechaLimpia = fechaFin.substringBefore(".")
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = formatoEntrada.parse(fechaLimpia)

        date != null && date.time <= System.currentTimeMillis()
    } catch (e: Exception) {
        false
    }
}

@Composable
fun DetailInfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.width(165.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp
            )
        }
    }
}