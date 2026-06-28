package com.example.lapuja.data.remote

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    val password: String
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
    val fechaFin: String?
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