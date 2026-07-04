package com.example.torque.network

import com.example.torque.network.models.LoginResponse
import com.example.torque.network.models.PinLoginRequest
import com.example.torque.network.models.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TorqueApiService {

    @POST("api/v1/auth/login-pin")
    suspend fun loginPin(@Body request: PinLoginRequest): Response<LoginResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(): Response<UsuarioResponse>
}
