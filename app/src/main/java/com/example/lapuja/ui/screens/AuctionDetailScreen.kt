package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.Bid
import com.example.lapuja.data.remote.*
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.lapuja.ui.components.AppProfileImage

@Composable
fun AuctionDetailScreen(
    subastaId: Long,
    prefs: SharedPreferences,
    historial: MutableList<Bid>,
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

    val usuarioId = prefs.getLong("usuarioId", 0L)

    fun cargarSubasta() {
        scope.launch {
            try {
                cargando = true

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
                    }
                } else {
                    mensaje = "No se pudo cargar la subasta."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(subastaId) {
        cargarSubasta()
    }

    fun realizarPuja(monto: Double) {
        val item = subasta ?: return

        if (usuarioId == 0L) {
            mensaje = "Debes iniciar sesión para pujar."
            return
        }

        if (item.estado != "ACTIVA") {
            mensaje = "La subasta no está activa."
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
                    cargarSubasta()
                } else {
                    mensaje = resultado?.mensaje ?: "No se pudo realizar la puja."
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

    val puedePujar = auction.estado == "ACTIVA"

    val estadoColor =
        when (auction.estado) {
            "ACTIVA" -> Color(0xFF4CAF50)
            "PROGRAMADA" -> Color(0xFFFFC107)
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
                ImagenSubasta(
                    imagen = auction.imagen,
                    nombre = auction.nombre
                )
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
                        text = auction.estado,
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
                    Text(text = "Oferta actual", fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "$${auction.precioActual}", fontSize = 34.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Ofertas realizadas: ${auction.ofertas}")
                    Text(text = "Ganador actual: ${auction.ganador}")
                }
            }

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

                            cargarSubasta()
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Saldo disponible",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$${prefs.getFloat("saldo", 10000f).toDouble()}",
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            realizarPuja(auction.precioActual + 0.5)
                        },
                        enabled = puedePujar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (puedePujar) "🔥 Pujar +$0.50" else "Puja no disponible"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = montoPersonalizado,
                        onValueChange = {
                            montoPersonalizado = it
                            mensaje = ""
                        },
                        label = { Text("Monto personalizado") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val monto = montoPersonalizado.toDoubleOrNull()

                            if (monto == null) {
                                mensaje = "Ingrese un monto válido."
                                return@OutlinedButton
                            }

                            realizarPuja(monto)
                        },
                        enabled = puedePujar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Ofertar monto personalizado")
                    }
                }
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensaje,
                    color = if (
                        mensaje.contains("correctamente") ||
                        mensaje.contains("guardada") ||
                        mensaje.contains("eliminada")
                    ) {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            if (auction.estado == "FINALIZADA") {
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