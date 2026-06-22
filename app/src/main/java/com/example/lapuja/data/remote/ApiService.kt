package com.example.lapuja.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @POST("api/usuarios/login")
    suspend fun loginUsuario(
        @Body request: LoginRequest
    ): Response<UsuarioResponse>

    @GET("api/subastas")
    suspend fun listarSubastas(): Response<List<SubastaResponse>>

    @POST("api/subastas")
    suspend fun crearSubasta(
        @Body request: SubastaRequest
    ): Response<SubastaResponse>

    @POST("api/pujas")
    suspend fun crearPuja(
        @Body request: PujaRequest
    ): Response<PujaResponse>
}