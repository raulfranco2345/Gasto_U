package com.example.gastou.domain.repository

import com.example.gastou.domain.model.User
import com.example.gastou.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/** Contrato de autenticación y perfil. La implementación usa la API y DataStore. */
interface AuthRepository {
    /** Sesión actual (null si no hay usuario autenticado). */
    val session: Flow<UserSession?>

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(nombre: String, email: String, password: String): Result<User>
    suspend fun refreshProfile(): Result<User>
    suspend fun updateProfile(nombre: String, universidad: String): Result<User>

    /** Cierra sesión y borra los datos locales del usuario. */
    suspend fun logout()
}
