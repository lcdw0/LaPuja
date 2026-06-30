package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.NotificacionResponse
import com.example.lapuja.ui.notifications.NotificationViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun NotificationsScreen(
    prefs: SharedPreferences,
    navController: NavController,
    notificationViewModel: NotificationViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val usuarioId = obtenerUsuarioIdNotificaciones(prefs)
    val notificaciones = notificationViewModel.notificaciones
    val cargando = notificationViewModel.cargando
    val mensaje = notificationViewModel.mensaje

    LaunchedEffect(usuarioId) {
        notificationViewModel.cargarNotificaciones(usuarioId)
    }

    LaunchedEffect(mensaje) {
        if (mensaje.isNotBlank()) {
            snackbarHostState.showSnackbar(mensaje)
            notificationViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notificaciones",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            notificationViewModel.cargarNotificaciones(usuarioId)
                        }
                    ) {
                        Text("Actualizar")
                    }
                }

                if (notificaciones.any { !it.leida }) {
                    TextButton(
                        onClick = {
                            notificationViewModel.marcarTodasComoLeidas(usuarioId)
                        }
                    ) {
                        Text("Marcar todas como leídas")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (cargando) {
                item {
                    NotificationsLoading()
                }
            } else if (notificaciones.isEmpty()) {
                item {
                    NotificationsEmptyState()
                }
            } else {
                items(
                    items = notificaciones,
                    key = { it.id }
                ) { notificacion ->

                    NotificationItem(
                        notificacion = notificacion,
                        onClick = {
                            notificationViewModel.marcarComoLeida(notificacion.id)
                            navegarDesdeNotificacion(
                                notificacion = notificacion,
                                navController = navController
                            )
                        },
                        onDelete = {
                            scope.launch {
                                notificationViewModel.eliminarNotificacion(notificacion.id)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notificacion: NotificacionResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor =
        if (notificacion.leida) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.width(10.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (!notificacion.leida) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notificacion.titulo,
                    fontSize = 17.sp,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notificacion.mensaje,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tiempoRelativoNotificacion(notificacion.fecha),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Gray
                )
            }
        }
    }
}

fun navegarDesdeNotificacion(
    notificacion: NotificacionResponse,
    navController: NavController
) {
    when (notificacion.pantallaDestino) {
        "auction_detail" -> {
            notificacion.referenciaId?.let { subastaId ->
                navController.navigate("auction_detail/$subastaId")
            }
        }

        "wallet" -> {
            navController.navigate("wallet_history")
        }

        "public_profile" -> {
            notificacion.referenciaId?.let { usuarioId ->
                navController.navigate("public_profile/$usuarioId")
            }
        }
    }
}

@Composable
fun NotificationsLoading() {
    Column {
        repeat(6) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp),
                shape = RoundedCornerShape(18.dp)
            ) {}

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun NotificationsEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔔",
                fontSize = 42.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No tienes notificaciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cuando tengas novedades, aparecerán aquí.",
                color = Color.Gray
            )
        }
    }
}

fun tiempoRelativoNotificacion(fecha: String?): String {
    if (fecha.isNullOrBlank()) return ""

    return try {
        val fechaLimpia = fecha.substringBefore(".")
        val fechaNotificacion = LocalDateTime.parse(fechaLimpia)
        val ahora = LocalDateTime.now()

        val diferencia = Duration.between(fechaNotificacion, ahora)

        when {
            diferencia.toMinutes() < 1 -> "Ahora"
            diferencia.toMinutes() < 60 -> "Hace ${diferencia.toMinutes()} min"
            diferencia.toHours() < 24 -> "Hace ${diferencia.toHours()} h"
            diferencia.toDays() == 1L -> "Ayer"
            diferencia.toDays() < 7 -> "Hace ${diferencia.toDays()} días"
            else -> fechaLimpia.substringBefore("T")
        }
    } catch (e: Exception) {
        fecha.replace("T", " ").substringBefore(".")
    }
}

fun obtenerUsuarioIdNotificaciones(prefs: SharedPreferences): Long {
    return when (val id = prefs.all["usuarioId"]) {
        is Long -> id.toLong()
        is Int -> id.toLong()
        is String -> id.toLongOrNull() ?: 0L
        else -> 0L
    }
}