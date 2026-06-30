package com.example.lapuja.components

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.lapuja.ui.notifications.NotificationViewModel

@Composable
fun NotificationBell(
    prefs: SharedPreferences,
    navController: NavController,
    notificationViewModel: NotificationViewModel
) {
    val usuarioId = obtenerUsuarioIdNotificationBell(prefs)
    val noLeidas = notificationViewModel.noLeidas

    LaunchedEffect(usuarioId) {
        notificationViewModel.cargarContador(usuarioId)
    }

    IconButton(
        onClick = {
            navController.navigate("notifications")
        }
    ) {
        if (noLeidas > 0) {
            BadgedBox(
                badge = {
                    Badge {
                        Text(
                            text = if (noLeidas > 99) "99+" else noLeidas.toString()
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones"
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones"
            )
        }
    }
}

fun obtenerUsuarioIdNotificationBell(prefs: SharedPreferences): Long {
    return when (val id = prefs.all["usuarioId"]) {
        is Long -> id
        is Int -> id.toLong()
        is String -> id.toLongOrNull() ?: 0L
        else -> 0L
    }
}