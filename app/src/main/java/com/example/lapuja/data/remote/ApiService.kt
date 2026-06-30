package com.example.lapuja.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.example.lapuja.data.model.dashboard.DashboardResumenResponse
import com.example.lapuja.data.model.dashboard.DashboardGraficasResponse
import com.example.lapuja.data.model.dashboard.DashboardActividadResponse

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

    @Multipart
    @POST("api/imagenes/chat")
    suspend fun subirImagenChat(
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

    @GET("api/dashboard/resumen/{usuarioId}")
    suspend fun obtenerDashboardResumen(
        @Path("usuarioId") usuarioId: Long
    ): DashboardResumenResponse

    @GET("api/dashboard/graficas/{usuarioId}")
    suspend fun obtenerDashboardGraficas(
        @Path("usuarioId") usuarioId: Long
    ): DashboardGraficasResponse

    @GET("api/dashboard/actividad/{usuarioId}")
    suspend fun obtenerDashboardActividad(
        @Path("usuarioId") usuarioId: Long
    ): List<DashboardActividadResponse>

    @GET("api/usuarios/{id}/perfil-publico")
    suspend fun obtenerPerfilPublico(
        @Path("id") id: Long
    ): Response<PerfilPublicoResponse>

    @GET("api/usuarios/{id}/subastas/activas")
    suspend fun obtenerSubastasActivasPublicas(
        @Path("id") id: Long
    ): Response<PublicAuctionListResponse>

    @GET("api/usuarios/{id}/subastas/finalizadas")
    suspend fun obtenerSubastasFinalizadasPublicas(
        @Path("id") id: Long
    ): Response<PublicAuctionListResponse>

    @GET("api/usuarios/{id}/subastas/vendidas")
    suspend fun obtenerSubastasVendidasPublicas(
        @Path("id") id: Long
    ): Response<PublicAuctionListResponse>

    @GET("api/notificaciones/usuario/{usuarioId}")
    suspend fun obtenerNotificaciones(
        @Path("usuarioId") usuarioId: Long
    ): Response<NotificacionesListResponse>

    @GET("api/notificaciones/usuario/{usuarioId}/contador")
    suspend fun obtenerContadorNotificaciones(
        @Path("usuarioId") usuarioId: Long
    ): Response<NotificacionesContadorResponse>

    @PUT("api/notificaciones/{id}/leer")
    suspend fun marcarNotificacionLeida(
        @Path("id") id: Long
    ): Response<NotificacionSimpleResponse>

    @PUT("api/notificaciones/usuario/{usuarioId}/leer-todas")
    suspend fun marcarTodasNotificacionesLeidas(
        @Path("usuarioId") usuarioId: Long
    ): Response<NotificacionSimpleResponse>

    @DELETE("api/notificaciones/{id}")
    suspend fun eliminarNotificacion(
        @Path("id") id: Long
    ): Response<NotificacionSimpleResponse>

    @GET("api/email/verificar")
    suspend fun verificarCorreo(
        @Query("token") token: String
    ): Response<Map<String, Any>>

    @POST("api/email/restablecer-password")
    suspend fun restablecerPassword(
        @Body request: ResetPasswordRequest
    ): Response<Map<String, Any>>

    @POST("api/email/solicitar-recuperacion")
    suspend fun solicitarRecuperacion(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/chat/conversaciones")
    suspend fun crearConversacionChat(
        @Query("subastaId") subastaId: Long,
        @Query("compradorId") compradorId: Long,
        @Query("vendedorId") vendedorId: Long
    ): Response<ChatConversacionResponse>

    @GET("api/chat/conversaciones/{usuarioId}")
    suspend fun listarConversacionesChat(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<ChatConversacionResponse>>

    @GET("api/chat/conversaciones/{conversacionId}/mensajes")
    suspend fun listarMensajesChat(
        @Path("conversacionId") conversacionId: Long
    ): Response<List<ChatMensajeResponse>>

    @POST("api/chat/conversaciones/{conversacionId}/mensajes")
    suspend fun enviarMensajeChat(
        @Path("conversacionId") conversacionId: Long,
        @Body request: ChatMensajeRequest
    ): Response<ChatMensajeResponse>

    @PUT("api/chat/conversaciones/{conversacionId}/leer")
    suspend fun marcarMensajesChatComoLeidos(
        @Path("conversacionId") conversacionId: Long,
        @Query("usuarioId") usuarioId: Long
    ): Response<Unit>
}