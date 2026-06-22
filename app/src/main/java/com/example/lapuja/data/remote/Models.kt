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

data class UsuarioResponse(
    val ok: Boolean,
    val mensaje: String,
    val id: Long?,
    val nombre: String?,
    val correo: String?
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
    val ok: Boolean,
    val mensaje: String
)