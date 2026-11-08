package com.example.gastou.data.remote.dto

/* Objetos de petición y respuesta de /api/auth */

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val nombre: String,
    val email: String,
    val password: String
)

data class UpdateProfileRequestDto(
    val nombre: String,
    val universidad: String
)

data class UserDto(
    val id: String,
    val nombre: String,
    val email: String,
    val universidad: String?,
    val createdAt: String?
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

/** Cuerpo de error que devuelve la API: { "message": "...", "details": [...] } */
data class ApiErrorDto(
    val message: String?,
    val details: Any?
)
