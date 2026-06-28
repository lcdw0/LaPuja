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

    @POST("api/subastas")
    suspend fun crearSubasta(@Body request: SubastaRequest): Response<SubastaResponse>

    @PUT("api/subastas/{id}/finalizar")
    suspend fun finalizarSubasta(@Path("id") id: Long): Response<SubastaResponse>

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
}