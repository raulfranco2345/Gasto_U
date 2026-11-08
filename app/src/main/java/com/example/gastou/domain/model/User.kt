package com.example.gastou.domain.model

/** Usuario autenticado (lo que se muestra en el perfil). */
data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val universidad: String,
    val createdAt: String
)

/** Sesión guardada en DataStore: usuario + token JWT emitido por la API. */
data class UserSession(
    val token: String,
    val user: User
)
