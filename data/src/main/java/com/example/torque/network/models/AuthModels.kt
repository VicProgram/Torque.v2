package com.example.torque.network.models

import com.google.gson.annotations.SerializedName

data class PinLoginRequest(
    @SerializedName("pin_code") val pinCode: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("id_usuario") val idUsuario: String,
    @SerializedName("id_taller") val idTaller: String,
    val rol: String,
    val nombre: String,
)

data class UsuarioResponse(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean,
)
