package com.example.lapuja.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(@Body request: UsuarioRequest): Response<UsuarioResponse>

    @POST("api/usuarios/login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body request: UsuarioUpdateRequest
    ): Response<UsuarioResponse>

    @GET("api/subastas")
    suspend fun listarSubastas(): Response<List<SubastaResponse>>

    @GET("api/subastas/{id}")
    suspend fun obtenerSubasta(@Path("id") id: Long): Response<SubastaResponse>

    @GET("api/subastas/usuario/{usuarioId}")
    suspend fun listarSubastasPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<SubastaResponse>>

    @GET("api/subastas/activas")
    suspend fun listarSubastasActivas(): Response<List<SubastaResponse>>

    @GET("api/subastas/buscar")
    suspend fun buscarSubastas(
        @Query("texto") texto: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("min") min: Double? = null,
        @Query("max") max: Double? = null,
        @Query("estado") estado: String? = "TODAS",
        @Query("orden") orden: String? = "recientes"
    ): Response<List<SubastaResponse>>

    @POST("api/subastas")
    suspend fun crearSubasta(@Body request: SubastaRequest): Response<SubastaResponse>

    @PUT("api/subastas/{id}")
    suspend fun editarSubasta(
        @Path("id") id: Long,
        @Body request: SubastaRequest
    ): Response<SubastaResponse>

    @PUT("api/subastas/{id}/cancelar")
    suspend fun cancelarSubasta(
        @Path("id") id: Long,
        @Body request: CancelarSubastaRequest
    ): Response<SubastaResponse>

    @PUT("api/subastas/{id}/finalizar")
    suspend fun finalizarSubasta(@Path("id") id: Long): Response<SubastaResponse>

    @GET("api/subastas/{subastaId}/imagenes")
    suspend fun listarImagenesSubasta(
        @Path("subastaId") subastaId: Long
    ): Response<List<SubastaImagenResponse>>

    @POST("api/subastas/{subastaId}/imagenes")
    suspend fun agregarImagenSubasta(
        @Path("subastaId") subastaId: Long,
        @Body request: SubastaImagenRequest
    ): Response<SubastaImagenResponse>

    @PUT("api/subastas/{subastaId}/imagenes/{imagenId}/principal")
    suspend fun marcarImagenPrincipal(
        @Path("subastaId") subastaId: Long,
        @Path("imagenId") imagenId: Long
    ): Response<SubastaImagenResponse>

    @DELETE("api/subastas/{subastaId}/imagenes/{imagenId}")
    suspend fun eliminarImagenSubasta(
        @Path("subastaId") subastaId: Long,
        @Path("imagenId") imagenId: Long
    ): Response<ApiResponse>

    @PUT("api/subastas/{subastaId}/imagenes/reordenar")
    suspend fun reordenarImagenesSubasta(
        @Path("subastaId") subastaId: Long,
        @Body idsOrdenados: List<Long>
    ): Response<List<SubastaImagenResponse>>

    @POST("api/pujas")
    suspend fun crearPuja(@Body request: PujaRequest): Response<PujaResponse>

    @GET("api/pujas")
    suspend fun listarPujas(): Response<List<PujaResponse>>

    @GET("api/pujas/usuario/{usuarioId}")
    suspend fun listarPujasPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<PujaResponse>>

    @GET("api/pujas/subasta/{subastaId}")
    suspend fun listarPujasPorSubasta(
        @Path("subastaId") subastaId: Long
    ): Response<List<PujaResponse>>

    @POST("api/favoritos")
    suspend fun agregarFavorito(@Body request: FavoritoRequest): Response<FavoritoResponse>

    @GET("api/favoritos/usuario/{usuarioId}")
    suspend fun listarFavoritos(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<FavoritoResponse>>

    @DELETE("api/favoritos/{id}")
    suspend fun eliminarFavorito(@Path("id") id: Long): Response<Map<String, Any>>

    @Multipart
    @POST("api/imagenes/subasta")
    suspend fun subirImagenSubasta(
        @Part file: MultipartBody.Part
    ): Response<ImagenResponse>

    @Multipart
    @POST("api/imagenes/perfil")
    suspend fun subirImagenPerfil(
        @Part file: MultipartBody.Part
    ): Response<ImagenResponse>

    @POST("api/metodos-pago")
    suspend fun agregarMetodoPago(
        @Body request: MetodoPagoRequest
    ): Response<ApiResponse>

    @GET("api/metodos-pago/usuario/{usuarioId}")
    suspend fun listarMetodosPago(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<MetodoPagoResponse>>

    @DELETE("api/metodos-pago/{id}")
    suspend fun eliminarMetodoPago(
        @Path("id") id: Long
    ): Response<ApiResponse>

    @PUT("api/metodos-pago/{id}/principal")
    suspend fun marcarMetodoPrincipal(
        @Path("id") id: Long
    ): Response<ApiResponse>

    @POST("api/wallet/{usuarioId}/recargar")
    suspend fun recargarSaldo(
        @Path("usuarioId") usuarioId: Long,
        @Body request: RecargaRequest
    ): Response<WalletResponse>

    @GET("api/wallet/{usuarioId}/saldo")
    suspend fun obtenerSaldo(
        @Path("usuarioId") usuarioId: Long
    ): Response<WalletResponse>

    @GET("api/wallet/{usuarioId}/movimientos")
    suspend fun listarMovimientosWallet(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<WalletMovimientoResponse>>

    @GET("api/wallet/{usuarioId}/retenido")
    suspend fun obtenerSaldoRetenido(
        @Path("usuarioId") usuarioId: Long
    ): Response<SaldoRetenidoResponse>
}