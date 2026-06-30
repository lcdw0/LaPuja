package com.example.lapuja.ui.notifications

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lapuja.data.remote.NotificacionResponse
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    var notificaciones by mutableStateOf<List<NotificacionResponse>>(emptyList())
        private set

    var noLeidas by mutableLongStateOf(0L)
        private set

    var cargando by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf("")
        private set

    fun cargarNotificaciones(usuarioId: Long) {
        viewModelScope.launch {
            try {
                cargando = true
                mensaje = ""

                val response = RetrofitClient.api.obtenerNotificaciones(usuarioId)

                if (response.isSuccessful && response.body()?.ok == true) {
                    notificaciones = response.body()?.notificaciones ?: emptyList()
                    actualizarContadorLocal()
                } else {
                    mensaje = response.body()?.mensaje ?: "No se pudieron cargar las notificaciones."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    fun cargarContador(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.obtenerContadorNotificaciones(usuarioId)

                if (response.isSuccessful && response.body()?.ok == true) {
                    noLeidas = response.body()?.noLeidas ?: 0L
                }
            } catch (e: Exception) {
                noLeidas = 0L
            }
        }
    }

    fun marcarComoLeida(notificacionId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.marcarNotificacionLeida(notificacionId)

                if (response.isSuccessful) {
                    notificaciones = notificaciones.map {
                        if (it.id == notificacionId) it.copy(leida = true) else it
                    }

                    actualizarContadorLocal()
                }
            } catch (e: Exception) {
                mensaje = "No se pudo marcar la notificación como leída."
            }
        }
    }

    fun marcarTodasComoLeidas(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.marcarTodasNotificacionesLeidas(usuarioId)

                if (response.isSuccessful) {
                    notificaciones = notificaciones.map {
                        it.copy(leida = true)
                    }

                    noLeidas = 0L
                    mensaje = "Todas las notificaciones fueron marcadas como leídas."
                } else {
                    mensaje = "No se pudieron marcar como leídas."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            }
        }
    }

    fun eliminarNotificacion(notificacionId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.eliminarNotificacion(notificacionId)

                if (response.isSuccessful) {
                    notificaciones = notificaciones.filter {
                        it.id != notificacionId
                    }

                    actualizarContadorLocal()
                    mensaje = "Notificación eliminada."
                } else {
                    mensaje = "No se pudo eliminar la notificación."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            }
        }
    }

    fun limpiarMensaje() {
        mensaje = ""
    }

    private fun actualizarContadorLocal() {
        noLeidas = notificaciones.count { !it.leida }.toLong()
    }
}