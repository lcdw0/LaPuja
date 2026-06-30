package com.example.lapuja.data.remote

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val telefono: String,
    val ciudad: String
)

data class LoginRequest(
    val correo: String,
    val password: String
)

data class UsuarioUpdateRequest(
    val nombre: String,
    val correo: String,
    val passwordActual: String?,
    val password: String?,
    val confirmarPassword: String?,
    val fotoPerfil: String?,
    val telefono: String?,
    val ciudad: String?,
    val biografia: String?
)

data class UsuarioResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val id: Long?,
    val nombre: String?,
    val correo: String?,
    val fotoPerfil: String?,
    val telefono: String?,
    val ciudad: String?,
    val biografia: String?,
    val fechaRegistro: String?
)

data class SubastaRequest(
    val nombre: String,
    val descripcion: String,
    val precioInicial: Double,
    val categoria: String,
    val imagen: String,
    val usuarioId: Long,
    val fechaFin: String
)

data class SubastaImagenRequest(
    val url: String
)

data class SubastaImagenResponse(
    val id: Long?,
    val subastaId: Long?,
    val url: String?,
    val orden: Int?,
    val principal: Boolean?
)

data class SubastaResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precioInicial: Double,
    val precioActual: Double,
    val categoria: String,
    val imagen: String?,
    val estado: String,
    val ofertas: Int,
    val ganador: String,
    val ganadorId: Long?,
    val usuarioId: Long?,
    val fechaCreacion: String?,
    val fechaFin: String?,
    val imagenes: List<SubastaImagenResponse>? = emptyList()
)

data class PujaRequest(
    val usuarioId: Long,
    val subastaId: Long,
    val monto: Double
)

data class PujaResponse(
    val ok: Boolean? = null,
    val mensaje: String? = null,
    val id: Long? = null,
    val usuarioId: Long? = null,
    val subastaId: Long? = null,
    val monto: Double? = null,
    val fecha: String? = null,
    val saldo: Double? = null
)

data class FavoritoRequest(
    val usuarioId: Long,
    val subastaId: Long
)

data class FavoritoResponse(
    val id: Long?,
    val usuarioId: Long?,
    val subastaId: Long?
)

data class ImagenResponse(
    val ok: Boolean,
    val mensaje: String?,
    val url: String?
)

data class MetodoPagoRequest(
    val usuarioId: Long,
    val tipo: String,
    val marca: String,
    val titular: String,
    val ultimos4: String,
    val vencimiento: String,
    val principal: Boolean
)

data class MetodoPagoResponse(
    val id: Long?,
    val usuarioId: Long?,
    val tipo: String?,
    val marca: String?,
    val titular: String?,
    val ultimos4: String?,
    val vencimiento: String?,
    val principal: Boolean?
)

data class ApiResponse(
    val ok: Boolean,
    val mensaje: String?
)

data class WalletResponse(
    val ok: Boolean,
    val mensaje: String?,
    val saldo: Double?
)

data class WalletMovimientoResponse(
    val id: Long?,
    val usuarioId: Long?,
    val tipo: String?,
    val monto: Double?,
    val descripcion: String?,
    val fecha: String?
)

data class RecargaRequest(
    val metodoPagoId: Long,
    val monto: Double
)

data class SaldoRetenidoResponse(
    val ok: Boolean,
    val saldoDisponible: Double?,
    val totalRetenido: Double?,
    val items: List<SaldoRetenidoItemResponse>?
)

data class SaldoRetenidoItemResponse(
    val subastaId: Long?,
    val nombreSubasta: String?,
    val monto: Double?,
    val estado: String?,
    val fechaFin: String?
)

data class CancelarSubastaRequest(
    val usuarioId: Long
)

data class PerfilPublicoResponse(
    val ok: Boolean,
    val mensaje: String?,
    val id: Long?,
    val nombre: String?,
    val fotoPerfil: String?,
    val ciudad: String?,
    val biografia: String?,
    val fechaRegistro: String?,
    val cantidadVentas: Long?,
    val cantidadCompras: Long?,
    val subastasActivas: Long?,
    val subastasFinalizadas: Long?,
    val subastasVendidas: Long?,
    val reputacion: Double?,
    val promedioEstrellas: Double?
)

data class PublicAuctionListResponse(
    val ok: Boolean,
    val mensaje: String?,
    val subastas: List<SubastaResponse> = emptyList()
)

data class NotificacionResponse(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val referenciaId: Long?,
    val pantallaDestino: String,
    val leida: Boolean,
    val fecha: String?
)

data class NotificacionesListResponse(
    val ok: Boolean,
    val mensaje: String?,
    val notificaciones: List<NotificacionResponse> = emptyList()
)

data class NotificacionesContadorResponse(
    val ok: Boolean,
    val noLeidas: Long
)

data class NotificacionSimpleResponse(
    val ok: Boolean,
    val mensaje: String?
)

data class ResetPasswordRequest(
    val token: String,
    val nuevaPassword: String,
    val confirmarPassword: String
)

data class ChatConversacionResponse(
    val id: Long?,
    val subastaId: Long?,
    val subastaTitulo: String?,
    val compradorId: Long?,
    val compradorNombre: String?,
    val compradorFotoPerfil: String?,
    val vendedorId: Long?,
    val vendedorNombre: String?,
    val vendedorFotoPerfil: String?,
    val activo: Boolean?,
    val fechaCreacion: String?,
    val ultimoMensaje: String?,
    val fechaUltimoMensaje: String?,
    val mensajesNoLeidos: Long?,
    val otroUsuarioEnLinea: Boolean?,
    val otroUsuarioUltimaActividad: String?,
    val otroUsuarioFotoPerfil: String?
)

data class ChatMensajeResponse(
    val id: Long?,
    val conversacionId: Long?,
    val emisorId: Long?,
    val emisorNombre: String?,
    val contenido: String?,
    val tipoMensaje: String?,
    val imagenUrl: String?,
    val leido: Boolean?,
    val eliminado: Boolean?,
    val fechaEnvio: String?,
    val fechaEdicion: String?
)

data class ChatMensajeRequest(
    val emisorId: Long,
    val contenido: String? = null,
    val tipoMensaje: String = "TEXTO",
    val imagenUrl: String? = null
)